CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE embeddings (
                            id SERIAL PRIMARY KEY,
                            category TEXT,        -- Основная категория
                            subcategory TEXT,     -- Подкатегория
                            question TEXT,        -- Пример вопроса
                            answer TEXT,          -- Шаблонный ответ
                            priority TEXT,        -- Приоритет (например: высокий, средний, низкий)
                            audience TEXT,        -- Целевая аудитория (например: новые клиенты)
                            embedding vector(1024)  -- Векторное представление текста (из SciBox)
);