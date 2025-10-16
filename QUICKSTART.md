# ⚡ Быстрый старт

## 1️⃣ Development режим (рекомендуется)

### Запуск
```bash
./start-dev.sh
```

### Доступ
- 🌐 Frontend: http://localhost:5173
- 🔧 Backend: http://localhost:8080
- 🗄️ PostgreSQL: localhost:5000

### Остановка
```bash
./stop-dev.sh
```

### Логи
```bash
docker-compose -f docker-compose.dev.yml logs -f
```

---

## 2️⃣ Production режим

### Сборка и запуск
```bash
# Собрать frontend
cd frontend && npm install && npm run build && cd ..

# Или используй скрипт
./build-frontend.sh

# Запустить
docker-compose up -d
```

### Доступ
- 🌐 Frontend: http://localhost
- 🔧 Backend: http://localhost:8080

### Остановка
```bash
docker-compose down
```

---

## 3️⃣ Первая настройка

1. Открой http://localhost:5173
2. Перейди в **Настройки** (⚙️ в меню)
3. Укажи **API Base URL**: `http://localhost:8080`
4. Нажми **"Сохранить изменения"**

---

## 4️⃣ Загрузка базы знаний

1. Подготовь Excel файл с колонками:
   - `Вопрос`
   - `Ответ`
   - `Категория`

2. В **Настройках** нажми **"Загрузить БД (Excel)"**
3. Выбери файл
4. Дождись уведомления "Успешно загружено N записей"

---

## 5️⃣ Работа с приложением

### Рабочий стол оператора
- **История переписки** - диалоги с клиентами
- **Симуляция клиента** - для тестирования
- **Подсказки** - автоматический поиск по FAQ
- **Статистика** - метрики работы

### Вкладки тикетов
- 📥 **Входящие** - новые обращения
- ⚙️ **В работе** - активные диалоги
- ✅ **Закрытые** - завершенные

---

## 🐛 Проблемы?

### Не запускается dev режим
```bash
docker-compose -f docker-compose.dev.yml down
docker-compose -f docker-compose.dev.yml up -d --build
```

### Не работает hot reload
```bash
docker-compose -f docker-compose.dev.yml restart frontend
```

### Очистить все
```bash
docker-compose -f docker-compose.dev.yml down -v
docker system prune -a
```

---

## 📚 Полная документация

Смотри [README.md](./README.md)
