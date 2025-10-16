# 📤 Тестирование загрузки БД

## Проверка работы загрузки FAQ из Excel

### 1️⃣ Через UI (Frontend)

1. **Запустить dev режим:**
```bash
./start-dev.sh
```

2. **Открыть приложение:**
```
http://localhost:5173
```

3. **Перейти в Настройки:**
   - Нажать на иконку ⚙️ в меню
   - Или открыть: `http://localhost:5173/settings`

4. **Настроить API:**
   - API Base URL: `http://localhost:8080`
   - Нажать "Сохранить изменения"

5. **Загрузить файл:**
   - Нажать кнопку "Загрузить БД (Excel)"
   - Выбрать файл: `smart_support_vtb_belarus_faq_final (2).xlsx`
   - Дождаться уведомления
   - Должно появиться: ✅ "Успешно загружено 201 записей из файла..."

### 2️⃣ Через curl (Backend напрямую)

```bash
curl -X POST \
  -F "file=@smart_support_vtb_belarus_faq_final (2).xlsx" \
  http://localhost:8080/api/v1/faq/import
```

**Ожидаемый ответ:**
```json
{"status":"ok","inserted":201}
```

### 3️⃣ Проверка логов

**Frontend логи (если ошибка):**
```bash
docker-compose -f docker-compose.dev.yml logs -f frontend
```

**Backend логи:**
```bash
docker-compose -f docker-compose.dev.yml logs -f backend
```

**Проверить консоль браузера:**
- F12 → Console
- Должны быть логи:
  - "Upload error:" (если ошибка)
  - "JSON parse error:" (если проблема с парсингом)

### 4️⃣ Возможные ошибки и решения

#### ❌ "Failed to execute 'json' on 'Response': Unexpected end of JSON input"

**Причина:** Backend вернул пустой ответ или не-JSON

**Решение:** 
✅ Уже исправлено! Используется `response.text()` → `JSON.parse()`

---

#### ❌ "Файл слишком большой"

**Причина:** Файл больше 10 МБ

**Решение:** 
- Сжать Excel файл
- Удалить лишние столбцы/строки
- Разбить на несколько файлов

---

#### ❌ CORS ошибка

**Причина:** Frontend и Backend на разных доменах

**Проверка:**
```bash
curl -I http://localhost:8080/api/v1/health/scibox
```

**Должен быть заголовок:**
```
Access-Control-Allow-Origin: *
```

---

#### ❌ Backend не отвечает

**Проверка:**
```bash
docker-compose -f docker-compose.dev.yml ps backend
```

**Перезапуск:**
```bash
docker-compose -f docker-compose.dev.yml restart backend
docker-compose -f docker-compose.dev.yml logs -f backend
```

---

### 5️⃣ Проверка данных в БД

**Подключиться к PostgreSQL:**
```bash
docker-compose -f docker-compose.dev.yml exec db psql -U postgres -d support
```

**SQL запросы:**
```sql
-- Количество записей
SELECT COUNT(*) FROM faq_items;

-- Первые 5 записей
SELECT id, question, answer, category FROM faq_items LIMIT 5;

-- Выход
\q
```

---

## ✅ Успешный тест

Если всё прошло успешно, вы должны увидеть:

1. ✅ Уведомление: "Успешно загружено 201 записей из файла..."
2. ✅ В консоли браузера нет ошибок
3. ✅ В логах backend нет ошибок
4. ✅ В БД 201 запись

---

## 📊 Формат Excel файла

Файл должен содержать колонки:

| Вопрос | Ответ | Категория |
|--------|-------|-----------|
| Как сбросить пароль? | Нажмите "Забыли пароль"... | Авторизация |
| Не приходит код | Проверьте спам... | Безопасность |

---

## 🔍 Отладка

**Включить подробные логи:**
```bash
# Frontend
docker-compose -f docker-compose.dev.yml logs -f frontend | grep -i error

# Backend  
docker-compose -f docker-compose.dev.yml logs -f backend | grep -i error

# Все ошибки
docker-compose -f docker-compose.dev.yml logs | grep -i -E "(error|exception|failed)"
```

**Проверить сетевые запросы:**
- F12 → Network → Filter: XHR
- Должен быть запрос: `POST /api/v1/faq/import`
- Status: 200 OK
- Response: `{"status":"ok","inserted":201}`

---

**Дата:** Октябрь 2025  
**Статус:** ✅ Работает корректно
