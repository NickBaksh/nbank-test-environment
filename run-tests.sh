#!/usr/bin/env bash
# Результат: <RESULTS_ROOT>/<RUN_ID>_<TEST_PROFILE>/
#     ├── run.log              — полный лог прогона
#     ├── surefire-reports/    — XML/TXT результаты по классам
#     ├── site/ или reports/   — HTML-отчёт surefire
#     └── allure-results/      — сырые результаты Allure (если есть)
set -uo pipefail

TEST_PROFILE="${TEST_PROFILE:-api}"
RESULTS_ROOT="${RESULTS_ROOT:-./test-results}"
CMD_LOG="${CMD_LOG:-/app/logs/run.log}"
RUN_ID="${RUN_ID:-$(date -u +%Y-%m-%d_%H-%M-%SZ)}"
RUN_DIR="${RESULTS_ROOT}/${RUN_ID}_${TEST_PROFILE}"
KEEP_RUNS="${KEEP_RUNS:-0}"   # 0 — хранить все прогоны, N — только последние N

if ! mkdir -p "${RUN_DIR}"; then
  echo ">>> ERROR: cannot create ${RUN_DIR}. Set RESULTS_ROOT to a writable folder." >&2
  exit 2
fi

echo ">>> Run ${RUN_ID}, profile: ${TEST_PROFILE}"
echo ">>> Results: ${RUN_DIR}"

rm -rf target/surefire-reports target/site target/reports

if [ $# -gt 0 ]; then
  "$@"
  [ -f "${CMD_LOG}" ] && cp "${CMD_LOG}" "${RUN_DIR}/run.log"
else
  {
    echo ">>> Running tests with profile ${TEST_PROFILE}"
    mvn -B -ntp test -P "${TEST_PROFILE}"
    echo ">>> Generating surefire report"
    mvn -B -ntp -P "${TEST_PROFILE}" surefire-report:report-only
  } 2>&1 | tee "${RUN_DIR}/run.log"
fi

REPORTS_DIR="target/surefire-reports"
if ! ls "${REPORTS_DIR}"/TEST-*.xml >/dev/null 2>&1; then
  echo ">>> No test results found in ${REPORTS_DIR} — tests did not run (compilation error?)"
  TEST_EXIT=1
elif grep -h '<testsuite ' "${REPORTS_DIR}"/TEST-*.xml \
     | grep -oE '(failures|errors)="[0-9]+"' | grep -qv '="0"'; then
  TEST_EXIT=1
else
  TEST_EXIT=0
fi


for dir in surefire-reports reports site allure-results; do
  if [ -d "target/${dir}" ]; then
    cp -r "target/${dir}" "${RUN_DIR}/"
  fi
done

# Удаляем старые прогоны, если задан KEEP_RUNS
if [ "${KEEP_RUNS}" -gt 0 ]; then
  ls -1dt "${RESULTS_ROOT}"/*/ 2>/dev/null | tail -n +$((KEEP_RUNS + 1)) | while read -r old; do
    rm -rf "${old}"
  done
fi

REPORT=$(find "${RUN_DIR}" -maxdepth 2 -name 'surefire*.html' 2>/dev/null | head -n 1)
echo ">>> Done (exit code ${TEST_EXIT}). Report: ${REPORT:-not found, see ${RUN_DIR}}"
exit ${TEST_EXIT}