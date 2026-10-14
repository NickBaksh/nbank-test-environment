#!/usr/bin/env bash
# Публикация локально собранного образа с тестами в Docker Hub.
#
# Использование:
#   export DOCKERHUB_USERNAME=<логин>
#   export DOCKERHUB_TOKEN=<access token>
#   ./push-tests.sh                 # тег latest
#   ./push-tests.sh 1.0.0           # свой тег
#
# Переменные можно положить в файл .env рядом со скриптом (он должен быть в .gitignore).
set -euo pipefail

# ---------- Настройки ----------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Подхватываем .env, если он есть (не перезаписывая уже заданные переменные окружения)
if [ -f "${SCRIPT_DIR}/.env" ]; then
  while IFS='=' read -r key value; do
    [[ -z "${key}" || "${key}" =~ ^[[:space:]]*# ]] && continue
    key="$(echo "${key}" | xargs)"
    value="$(echo "${value}" | sed -e 's/^["'\'']//' -e 's/["'\'']$//')"
    [ -z "${!key:-}" ] && export "${key}=${value}"
  done < "${SCRIPT_DIR}/.env"
fi

LOCAL_IMAGE="${LOCAL_IMAGE:-nbank-test-environment}"   # имя образа после docker build -t ...
IMAGE_NAME="${IMAGE_NAME:-nbank-tests}"                # имя репозитория в Docker Hub
TAG="${1:-${TAG:-latest}}"
DOCKERHUB_USERNAME="${DOCKERHUB_USERNAME:-}"
DOCKERHUB_TOKEN="${DOCKERHUB_TOKEN:-}"

# ---------- Проверки ----------
if [ -z "${DOCKERHUB_USERNAME}" ]; then
  echo "ERROR: DOCKERHUB_USERNAME is not set" >&2
  exit 1
fi
if [ -z "${DOCKERHUB_TOKEN}" ]; then
  echo "ERROR: DOCKERHUB_TOKEN is not set (export it or put it into .env)" >&2
  exit 1
fi
if ! docker image inspect "${LOCAL_IMAGE}" >/dev/null 2>&1; then
  echo "ERROR: local image '${LOCAL_IMAGE}' not found. Build it first:" >&2
  echo "  docker build -t ${LOCAL_IMAGE} ." >&2
  exit 1
fi

REMOTE_IMAGE="${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${TAG}"

# Выходим из Docker Hub в конце, даже если что-то упало — токен не остаётся в системе
trap 'docker logout >/dev/null 2>&1 || true' EXIT

# ---------- 1. Логин по токену ----------
echo ">>> Logging in to Docker Hub as ${DOCKERHUB_USERNAME}"
# Токен передаётся через stdin: не попадает в историю команд и в список процессов
printf '%s' "${DOCKERHUB_TOKEN}" | docker login --username "${DOCKERHUB_USERNAME}" --password-stdin

# ---------- 2. Тегирование ----------
echo ">>> Tagging ${LOCAL_IMAGE} -> ${REMOTE_IMAGE}"
docker tag "${LOCAL_IMAGE}" "${REMOTE_IMAGE}"

# ---------- 3. Пуш ----------
echo ">>> Pushing ${REMOTE_IMAGE}"
docker push "${REMOTE_IMAGE}"

# ---------- 4. Итог ----------
echo
echo ">>> Done! Image published: ${REMOTE_IMAGE}"
echo ">>> Repository: https://hub.docker.com/r/${DOCKERHUB_USERNAME}/${IMAGE_NAME}"
echo ">>> Pull it with:"
echo "    docker pull ${REMOTE_IMAGE}"