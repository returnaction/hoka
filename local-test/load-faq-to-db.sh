#!/bin/bash

# Прямая загрузка FAQ данных в PostgreSQL через API бэкенда
# Требует запущенный бэкенд для получения embeddings

BASE_URL="http://localhost:8080"
export PGPASSWORD="postgres"

echo "🔄 Импорт FAQ данных через Spring Boot API..."
echo ""

# Функция для получения embedding от бэка
get_embedding() {
    local text="$1"
    curl -s -X POST "$BASE_URL/api/v1/embedding" \
        -H "Content-Type: application/json" \
        -d "{\"text\":\"$text\"}" | jq -c '.'
}

# Функция для вставки записи с embedding
insert_faq() {
    local category="$1"
    local subcategory="$2"
    local question="$3"
    local answer="$4"
    local priority="$5"
    local audience="$6"
    
    echo "📝 Обрабатываем: $question"
    
    # Получаем embedding для вопроса
    local embedding=$(get_embedding "$question")
    
    if [ -z "$embedding" ] || [ "$embedding" == "null" ]; then
        echo "❌ Ошибка получения embedding для: $question"
        return 1
    fi
    
    # Преобразуем массив в формат pgvector
    local vec_literal=$(echo "$embedding" | jq -r 'map(tostring) | join(",")')
    vec_literal="[$vec_literal]"
    
    # Вставляем в базу
    docker exec -e PGPASSWORD=postgres -i pgvector-demo psql -U postgres -d assist <<EOSQL
INSERT INTO faq_embeddings (category, subcategory, question, priority, audience, answer, embedding)
VALUES (
    '$category',
    '$subcategory',
    E'$(echo "$question" | sed "s/'/''/g")',
    '$priority',
    '$audience',
    E'$(echo "$answer" | sed "s/'/''/g")',
    CAST('$vec_literal' AS vector)
);
EOSQL
    
    if [ $? -eq 0 ]; then
        echo "✅ Добавлено"
    else
        echo "❌ Ошибка вставки"
    fi
    echo ""
}

# Импортируем данные из JSON файла
jq -c '.[]' demo-faq-data.json | while read -r item; do
    category=$(echo "$item" | jq -r '.category')
    subcategory=$(echo "$item" | jq -r '.subcategory')
    question=$(echo "$item" | jq -r '.question')
    answer=$(echo "$item" | jq -r '.answer')
    priority=$(echo "$item" | jq -r '.priority')
    audience=$(echo "$item" | jq -r '.audience')
    
    insert_faq "$category" "$subcategory" "$question" "$answer" "$priority" "$audience"
    
    # Небольшая задержка чтобы не перегружать API
    sleep 0.5
done

echo "🎉 Импорт завершен!"
echo ""
echo "Проверка количества записей:"
docker exec -e PGPASSWORD=postgres pgvector-demo psql -U postgres -d assist -c "SELECT COUNT(*) FROM faq_embeddings;"
