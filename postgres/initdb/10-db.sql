CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE faq_embeddings (
                            id SERIAL PRIMARY KEY,
                            category TEXT,          -- Основная категория
                            subcategory TEXT,       -- Подкатегория
                            question TEXT NOT NULL, -- Пример вопроса
                            priority TEXT,          -- Приоритет (например: высокий, средний, низкий)
                            audience TEXT,          -- Целевая аудитория (например: новые клиенты)
                            answer TEXT NOT NULL,            -- Шаблонный ответ
                            embedding vector(1024) NOT NULL  -- Векторное представление текста (из SciBox)
);

CREATE INDEX IF NOT EXISTS idx_faq_cat     ON faq_embeddings (category);
CREATE INDEX IF NOT EXISTS idx_faq_subcat  ON faq_embeddings (subcategory);