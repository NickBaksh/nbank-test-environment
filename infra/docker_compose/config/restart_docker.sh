#!/bin/bash

echo ">>> Остановить Docker Compose"
docker compose down

echo ">>> Docker pull все образы браузеров"

# Конфиг браузеров под архитектуру машины (его же читает Selenoid в docker-compose.yml)
case "$(uname -m)" in
  x86_64|amd64) default_browsers=browsers-amd64.json ;;
  *)            default_browsers=browsers.json ;;
esac
export SELENOID_BROWSERS="${SELENOID_BROWSERS:-$default_browsers}"
json_file="./config/${SELENOID_BROWSERS}"

# Проверяем, что jq установлен
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    exit 1
fi

# Извлекаем все значения .image через jq
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")

# Пробегаем по каждому образу и выполняем docker pull
for image in $images; do
    echo "Pulling $image..."
    docker pull "$image"
done

echo ">>> Запуск Docker Compose"
docker compose up -d --wait