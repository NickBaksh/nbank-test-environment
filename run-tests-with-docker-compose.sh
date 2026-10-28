#!/usr/bin/env bash
# =====================================================================
# Полный прогон API и UI тестов на окружении из Docker Compose.
#
#   ./run-tests-with-docker-compose.sh
#
# Что делает:
#   1. Поднимает окружение (docker compose up -d) и ждёт готовности сервисов
#   2. Собирает образ с тестами
#   3. Запускает API-тесты, затем UI-тесты в контейнере (docker run --rm)
#   4. Собирает Allure-отчёт с историей прошлых прогонов (если установлен allure CLI)
#   5. Останавливает окружение — всегда, даже если тесты упали
#
# Настройки (необязательно, через переменные окружения):
#   COMPOSE_FILE   путь к docker-compose.yml       (по умолчанию ./docker-compose.yml)
#   TEST_IMAGE     имя образа с тестами            (по умолчанию nbank-test-environment)
#   PROFILES       какие тесты запускать           (по умолчанию "api ui")
#   SKIP_BUILD=1   не пересобирать образ с тестами
#   KEEP_ENV=1     не останавливать окружение после тестов (для отладки)
#   ALLURE_REPORT_DIR  куда класть Allure-отчёт    (по умолчанию ./allure-report)
#   SKIP_ALLURE=1  не генерировать Allure-отчёт
# =====================================================================
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

COMPOSE_FILE="${COMPOSE_FILE:-${SCRIPT_DIR}/infra/docker_compose/docker-compose.yml}"
TEST_IMAGE="${TEST_IMAGE:-nbank-test-environment}"
PROFILES="${PROFILES:-api ui}"
NETWORK="${NETWORK:-nbank-network}"
RESULTS_DIR="${SCRIPT_DIR}/test-results"
# Общий RUN_ID для всех профилей: папки прогона <RUN_ID>_api и <RUN_ID>_ui
RUN_ID="${RUN_ID:-$(date -u +%Y-%m-%d_%H-%M-%SZ)}"
ALLURE_REPORT_DIR="${ALLURE_REPORT_DIR:-${SCRIPT_DIR}/allure-report}"
WAIT_TIMEOUT="${WAIT_TIMEOUT:-180}"


APIBASEURL="${APIBASEURL:-http://backend:4111}"
UIBASEURL="${UIBASEURL:-http://nginx}"
# Конфиг браузеров Selenoid под архитектуру машины: образ Chrome для arm64 не запускается на x86_64
case "$(uname -m)" in
  x86_64|amd64) DEFAULT_BROWSERS=browsers-amd64.json ;;
  *)            DEFAULT_BROWSERS=browsers.json ;;
esac
export SELENOID_BROWSERS="${SELENOID_BROWSERS:-${DEFAULT_BROWSERS}}"
SELENOID_URL="${SELENOID_URL:-http://selenoid:4444}"
SELENOID_UI_URL="${SELENOID_UI_URL:-http://selenoid-ui:8080}"
DB_URL="${DB_URL:-jdbc:postgresql://postgres:5432/nbank}"
WIREMOCK_HOST="${WIREMOCK_HOST:-wiremock}"
DB_CHECKS_ENABLED="${DB_CHECKS_ENABLED:-false}"

# ---------- Оформление вывода ----------
step() { echo; echo "==================== $* ===================="; }
info() { echo ">>> $*"; }
fail() { echo "❌ $*" >&2; }

compose() { docker compose -f "${COMPOSE_FILE}" "$@"; }

# ---------- Остановка окружения: выполняется ВСЕГДА ----------
cleanup() {
  local code=$?
  if [ "${KEEP_ENV:-0}" = "1" ]; then
    step "Окружение оставлено запущенным (KEEP_ENV=1)"
    info "Остановить вручную: docker compose -f ${COMPOSE_FILE} down"
  else
    step "Остановка окружения"
    compose down --remove-orphans || fail "Не удалось остановить окружение"
  fi
  exit "${code}"
}

wait_for() {
  local name="$1" url="$2" waited=0
  info "Ожидание ${name} (${url})..."
  until [ "$(curl -s -o /dev/null -w '%{http_code}' "${url}")" != "000" ]; do
    if [ "${waited}" -ge "${WAIT_TIMEOUT}" ]; then
      fail "${name} не ответил за ${WAIT_TIMEOUT} с"
      return 1
    fi
    sleep 3; waited=$((waited + 3))
  done
  info "${name} готов (${waited} с)"
}

# =====================================================================
step "0. Проверки"
if ! docker info >/dev/null 2>&1; then
  fail "Docker не запущен. Запустите Docker Desktop и повторите."
  exit 1
fi
if [ ! -f "${COMPOSE_FILE}" ]; then
  fail "Не найден ${COMPOSE_FILE}"
  info "Укажите путь: COMPOSE_FILE=путь/к/docker-compose.yml ./run-tests-with-docker-compose.sh"
  exit 1
fi
info "Compose-файл: ${COMPOSE_FILE}"
info "Образ с тестами: ${TEST_IMAGE}, профили: ${PROFILES}"

# С этого момента окружение будет остановлено при любом выходе из скрипта
trap cleanup EXIT
trap 'exit 130' INT TERM

# =====================================================================
step "1. Подготовка браузеров для Selenoid"
BROWSERS_JSON="$(dirname "${COMPOSE_FILE}")/config/${SELENOID_BROWSERS}"
info "Конфиг браузеров: ${SELENOID_BROWSERS}"
if [ -f "${BROWSERS_JSON}" ]; then
  for image in $(grep -o '"image"[[:space:]]*:[[:space:]]*"[^"]*"' "${BROWSERS_JSON}" | sed 's/.*"\([^"]*\)"$/\1/' | sort -u); do
    if docker image inspect "${image}" >/dev/null 2>&1; then
      info "Образ браузера уже есть: ${image}"
    else
      info "Скачиваю образ браузера: ${image}"
      docker pull "${image}" || fail "Не удалось скачать ${image}"
    fi
  done
else
  info "browsers.json не найден рядом с compose-файлом — пропускаю"
fi

# =====================================================================
step "2. Запуск окружения (docker compose up -d)"
compose up -d || { fail "Окружение не поднялось"; exit 1; }
compose ps

wait_for "Backend"  "http://localhost:4111" || exit 1
wait_for "Frontend (nginx)" "http://localhost:80" || exit 1
wait_for "Selenoid" "http://localhost:4444/status" || exit 1

# =====================================================================
if [ "${SKIP_BUILD:-0}" = "1" ]; then
  step "3. Сборка образа с тестами — пропущена (SKIP_BUILD=1)"
else
  step "3. Сборка образа с тестами"
  docker build -t "${TEST_IMAGE}" . || { fail "Сборка образа не удалась"; exit 1; }
fi

# =====================================================================
mkdir -p "${RESULTS_DIR}"
OVERALL_EXIT=0

for profile in ${PROFILES}; do
  step "4. Тесты: ${profile}"
  info "APIBASEURL=${APIBASEURL}"
  info "UIBASEURL=${UIBASEURL}"
  info "SELENOID_URL=${SELENOID_URL}"
  info "SELENOID_UI_URL=${SELENOID_UI_URL}"

  docker run --rm \
    --network "${NETWORK}" \
    -e TEST_PROFILE="${profile}" \
    -e RUN_ID="${RUN_ID}" \
    -e APIBASEURL="${APIBASEURL}" \
    -e UIBASEURL="${UIBASEURL}" \
    -e SELENOID_URL="${SELENOID_URL}" \
    -e SELENOID_UI_URL="${SELENOID_UI_URL}" \
    -e API_BASE_URL="${APIBASEURL}" \
    -e UI_BASE_URL="${UIBASEURL}" \
    -e UI_REMOTE="${SELENOID_URL}/wd/hub" \
    -e DB_URL="${DB_URL}" \
    -e WIREMOCK_HOST="${WIREMOCK_HOST}" \
    -e DB_CHECKS_ENABLED="${DB_CHECKS_ENABLED}" \
    -v "${RESULTS_DIR}:/app/results" \
    "${TEST_IMAGE}"
  code=$?

  if [ "${code}" -eq 0 ]; then
    info "✅ ${profile}: все тесты прошли"
  else
    fail "${profile}: есть упавшие тесты (код ${code})"
    OVERALL_EXIT=1
  fi
done

# =====================================================================
step "5. Allure-отчёт"
generate_allure_report() {
  if [ "${SKIP_ALLURE:-0}" = "1" ]; then
    info "Пропущено (SKIP_ALLURE=1)"
    return 0
  fi
  if ! command -v allure >/dev/null 2>&1; then
    info "allure CLI не установлен — пропускаю (brew install allure)"
    return 0
  fi

  # Собираем результаты всех профилей этого прогона в одну папку
  local merged="${RESULTS_DIR}/${RUN_ID}_allure-results"
  mkdir -p "${merged}"
  for profile in ${PROFILES}; do
    local src="${RESULTS_DIR}/${RUN_ID}_${profile}/allure-results"
    if [ -d "${src}" ]; then
      cp -r "${src}/." "${merged}/"
    else
      info "Нет allure-results для профиля ${profile}"
    fi
  done
  if [ -z "$(ls -A "${merged}")" ]; then
    info "Результатов Allure нет — отчёт не генерирую"
    return 0
  fi

  # История: allure generate --clean удаляет старый отчёт, поэтому history копируем до генерации
  if [ -d "${ALLURE_REPORT_DIR}/history" ]; then
    cp -r "${ALLURE_REPORT_DIR}/history" "${merged}/"
    info "История прошлых прогонов добавлена"
  else
    info "Истории ещё нет — это первый отчёт"
  fi

  allure generate "${merged}" -o "${ALLURE_REPORT_DIR}" --clean || {
    fail "Не удалось сгенерировать Allure-отчёт"
    return 0
  }
  info "Allure-отчёт: ${ALLURE_REPORT_DIR} (открыть: allure open ${ALLURE_REPORT_DIR})"
}
generate_allure_report

# =====================================================================
step "Итог"
info "Отчёты и логи: ${RESULTS_DIR}"
if [ "${OVERALL_EXIT}" -eq 0 ]; then
  info "✅ Все тесты прошли"
else
  fail "Есть упавшие тесты — смотрите отчёты в ${RESULTS_DIR}"
fi

exit "${OVERALL_EXIT}"