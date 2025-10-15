#!/bin/bash

# Скрипт для импорта демо FAQ данных в backend
# Конвертирует JSON в формат, который принимает бэк

BASE_URL="http://localhost:8080/api/v1"
JSON_FILE="demo-faq-data.json"

echo "🚀 Загрузка FAQ данных в базу..."
echo ""

# Читаем JSON и отправляем каждую запись
jq -c '.[]' "$JSON_FILE" | while read -r item; do
    category=$(echo "$item" | jq -r '.category')
    question=$(echo "$item" | jq -r '.question')
    
    echo "📝 Импортируем: $category - $question"
    
    # Отправляем через прямой API бэкенда
    # Бэк должен принимать JSON массив
    # Для теста отправим через embedding endpoint
    
    text=$(echo "$item" | jq -r '.question')
    
    # Временно используем direct DB insert через бэк
    # В продакшене нужно использовать /faq/import с Excel
    
done

echo ""
echo "✅ Данные готовы к импорту"
echo "⚠️  Для полноценного импорта нужен Excel файл и endpoint POST /api/v1/faq/import"
echo ""
echo "Альтернатива: вставьте данные напрямую в PostgreSQL:"
echo "docker exec -i pgvector-demo psql -U postgres -d support_chat << 'EOSQL'"
echo "-- Здесь SQL INSERT команды"
echo "EOSQL"
