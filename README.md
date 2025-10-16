# 🤖 Smart Support System

Интеллектуальная система поддержки клиентов с AI-ассистентом для операторов. Автоматический поиск по базе знаний и генерация ответов на основе FAQ.

## 📋 Оглавление

- [Технологический стек](#-технологический-стек)
- [Архитектура](#-архитектура)
- [Быстрый старт](#-быстрый-старт)
- [Режимы работы](#-режимы-работы)
- [Разработка](#-разработка)
- [Production](#-production)
- [API документация](#-api-документация)
- [Конфигурация](#-конфигурация)
- [Структура проекта](#-структура-проекта)

## 🛠 Технологический стек

### Frontend
- **React 19.2.0** - UI библиотека
- **TypeScript 5.9.3** - типизация
- **Material-UI v7.3.4** - компонентная библиотека
- **Redux Toolkit 2.9.0** - управление состоянием
- **Vite 6.4.0** - сборщик и dev-сервер
- **Emotion** - CSS-in-JS стили

### Backend
- **Spring Boot 3.5.6** - фреймворк
- **Java 21** - язык программирования
- **PostgreSQL + pgvector** - векторная БД для семантического поиска
- **Apache POI** - обработка Excel файлов

### Infrastructure
- **Docker & Docker Compose** - контейнеризация
- **Nginx** - reverse proxy для production
- **Vite Dev Server** - hot reload для разработки

## 🏗 Архитектура

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   React SPA     │─────>│  Spring Boot    │─────>│  PostgreSQL +   │
│  (MUI, Redux)   │      │     Backend     │      │    pgvector     │
└─────────────────┘      └─────────────────┘      └─────────────────┘
        │                         │
        │                         │
        v                         v
┌─────────────────┐      ┌─────────────────┐
│     Nginx       │      │   Scibox API    │
│  (Production)   │      │  (Embeddings)   │
└─────────────────┘      └─────────────────┘
```

## 🚀 Быстрый старт

### Предварительные требования

- **Docker** >= 20.10
- **Docker Compose** >= 2.0
- **Node.js** >= 20 (только для локальной разработки)
- **npm** >= 10

### Установка и запуск

1. **Клонировать репозиторий**
```bash
git clone https://github.com/returnaction/hoka.git
cd scibox-frontend
```

2. **Запустить в режиме разработки**
```bash
./start-dev.sh
```

3. **Открыть в браузере**
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- PostgreSQL: localhost:5000

## 🎯 Режимы работы

### Development Mode (Рекомендуется для разработки)

**Возможности:**
- ✅ Hot Module Replacement (HMR)
- ✅ Автоматическая перезагрузка при изменении кода
- ✅ Source maps для отладки
- ✅ Быстрый старт (~10 секунд)

**Запуск:**
```bash
# Запустить все сервисы
./start-dev.sh

# Остановить
./stop-dev.sh

# Посмотреть логи
docker-compose -f docker-compose.dev.yml logs -f

# Перезапустить конкретный сервис
docker-compose -f docker-compose.dev.yml restart frontend
```

**Доступные сервисы:**
- Frontend (Vite): `http://localhost:5173`
- Backend (Spring Boot): `http://localhost:8080`
- PostgreSQL: `localhost:5000`

### Production Mode

**Возможности:**
- ✅ Оптимизированная сборка
- ✅ Минификация и сжатие
- ✅ Nginx с gzip
- ✅ Кэширование статики

**Сборка и запуск:**
```bash
# 1. Собрать frontend локально
cd frontend
npm install
npm run build
cd ..

# 2. Собрать Docker images
docker-compose build

# 3. Запустить
docker-compose up -d

# 4. Остановить
docker-compose down
```

**Быстрая сборка frontend:**
```bash
./build-frontend.sh
```

**Доступ:**
- Frontend (Nginx): `http://localhost:80`
- Backend API: `http://localhost:8080`

## 💻 Разработка

### Структура frontend

```
frontend/
├── src/
│   ├── app/              # Корневой компонент приложения
│   ├── pages/            # Страницы приложения
│   │   ├── OperatorDesk/ # Рабочий стол оператора
│   │   └── SettingsPage/ # Настройки API
│   ├── widgets/          # Переиспользуемые виджеты
│   │   ├── HeaderBar.tsx
│   │   ├── TicketTabs.tsx
│   │   ├── TicketHeader.tsx
│   │   └── StatsStrip.tsx
│   ├── entities/         # Бизнес-сущности
│   │   └── dialog/       # Диалоги оператор-клиент
│   ├── shared/           # Общие утилиты
│   │   ├── hooks/        # React хуки
│   │   └── config/       # Конфигурация
│   └── api/              # API клиенты
├── public/               # Статические файлы
└── vite.config.ts        # Конфигурация Vite
```

### Разработка с hot reload

1. **Запустить dev окружение:**
```bash
./start-dev.sh
```

2. **Изменить код в `frontend/src/`**
   - Изменения применяются автоматически
   - Браузер обновляется без перезагрузки страницы

3. **Проверить в браузере:**
```bash
open http://localhost:5173
```

### Работа с API

**Настройка эндпоинтов:**
1. Открыть http://localhost:5173
2. Перейти в "Настройки" (⚙️)
3. Указать `API Base URL`: `http://localhost:8080`
4. Сохранить изменения

**Загрузка базы знаний:**
1. Подготовить Excel файл с FAQ (формат: вопрос, ответ, категория)
2. Перейти в "Настройки"
3. Нажать "Загрузить БД (Excel)"
4. Выбрать файл
5. Дождаться уведомления об успешной загрузке

### Локальная разработка без Docker

```bash
# Терминал 1 - Backend
cd smart-support-backend
./mvnw spring-boot:run

# Терминал 2 - Frontend
cd frontend
npm install
npm run dev

# Терминал 3 - PostgreSQL
docker run -d \
  --name pgvector \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=support \
  -p 5432:5432 \
  ankane/pgvector:latest
```

## 🏭 Production

### Деплой на сервер

```bash
# 1. Настроить переменные окружения
cp .env.example .env
vim .env

# 2. Собрать образы
docker-compose build

# 3. Запустить
docker-compose up -d

# 4. Проверить статус
docker-compose ps

# 5. Посмотреть логи
docker-compose logs -f
```

### Обновление приложения

```bash
# 1. Получить новый код
git pull

# 2. Пересобрать frontend
cd frontend && npm run build && cd ..

# 3. Пересобрать образы
docker-compose build

# 4. Перезапустить
docker-compose up -d --no-deps --build frontend

# 5. Очистить старые образы
docker image prune -f
```

### Мониторинг

```bash
# Статус контейнеров
docker-compose ps

# Логи всех сервисов
docker-compose logs -f

# Логи конкретного сервиса
docker-compose logs -f frontend
docker-compose logs -f backend
docker-compose logs -f db

# Использование ресурсов
docker stats

# Проверка здоровья backend
curl http://localhost:8080/api/v1/health/scibox
```

## 📡 API документация

### FAQ Management

**Загрузка FAQ из Excel**
```http
POST /api/v1/faq/import
Content-Type: multipart/form-data

file: <Excel файл>
```

Ответ:
```json
{
  "status": "ok",
  "inserted": 150
}
```

### Классификация и поиск

**Гибридный поиск (семантика + текст)**
```http
POST /api/v1/classify/hybrid?topK=3
Content-Type: application/json

{
  "text": "Как сбросить пароль?"
}
```

**Семантический поиск**
```http
POST /api/v1/classify/semantic?topK=3&threshold=0.6
Content-Type: application/json

{
  "text": "Забыл пароль от аккаунта"
}
```

**Классификация через Scibox**
```http
POST /api/v1/classify
Content-Type: application/json

{
  "text": "Вопрос клиента"
}
```

### Health Check

```http
GET /api/v1/health/scibox
```

Ответ:
```json
{
  "scibox": "ok"
}
```

## ⚙️ Конфигурация

### Frontend (.env)

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=Smart Support System
```

### Backend (application.properties)

```properties
# Database
spring.datasource.url=jdbc:postgresql://db:5432/support
spring.datasource.username=postgres
spring.datasource.password=postgres

# Scibox API
scibox.api.base-url=http://scibox:8000
scibox.api.embeddings-path=/v1/embeddings
scibox.api.model=bge-m3

# Server
server.port=8080
```

### Docker Compose

**Development (`docker-compose.dev.yml`):**
- Frontend: порт 5173, volume mounts для HMR
- Backend: порт 8080
- PostgreSQL: порт 5000

**Production (`docker-compose.yml`):**
- Frontend: порт 80, статические файлы через nginx
- Backend: порт 8080
- PostgreSQL: внутренний порт 5432

## 📂 Структура проекта

```
scibox-frontend/
├── frontend/                  # React приложение
│   ├── src/
│   │   ├── app/              # App root
│   │   ├── pages/            # Страницы
│   │   ├── widgets/          # UI компоненты
│   │   ├── entities/         # Бизнес-логика
│   │   └── shared/           # Утилиты
│   ├── public/               # Статика
│   ├── Dockerfile.simple     # Production build
│   ├── Dockerfile.dev        # Development build
│   └── package.json
│
├── smart-support-backend/    # Spring Boot API
│   ├── src/
│   │   └── main/
│   │       ├── java/         # Java код
│   │       └── resources/    # Конфиги
│   ├── Dockerfile
│   └── pom.xml
│
├── postgres/                 # PostgreSQL init scripts
│   └── init.sql
│
├── docker-compose.yml        # Production compose
├── docker-compose.dev.yml    # Development compose
├── start-dev.sh              # Запуск dev режима
├── stop-dev.sh               # Остановка dev режима
├── build-frontend.sh         # Сборка frontend
└── README.md                 # Эта документация
```

## 🐛 Troubleshooting

### Frontend не запускается

```bash
# Проверить логи
docker-compose -f docker-compose.dev.yml logs frontend

# Пересобрать контейнер
docker-compose -f docker-compose.dev.yml up -d --build frontend

# Очистить node_modules
docker-compose -f docker-compose.dev.yml exec frontend rm -rf node_modules
docker-compose -f docker-compose.dev.yml restart frontend
```

### Backend не подключается к БД

```bash
# Проверить статус PostgreSQL
docker-compose -f docker-compose.dev.yml ps db

# Проверить health check
docker-compose -f docker-compose.dev.yml exec db pg_isready -U postgres

# Пересоздать БД
docker-compose -f docker-compose.dev.yml down -v
docker-compose -f docker-compose.dev.yml up -d
```

### Hot reload не работает

```bash
# Проверить volume mounts
docker-compose -f docker-compose.dev.yml config

# Перезапустить frontend с rebuild
docker-compose -f docker-compose.dev.yml up -d --build frontend
```

### Очистка Docker

```bash
# Удалить все контейнеры
docker-compose -f docker-compose.dev.yml down

# Удалить с volumes
docker-compose -f docker-compose.dev.yml down -v

# Очистить неиспользуемые образы
docker image prune -a

# Полная очистка Docker
docker system prune -a --volumes
```

## 📝 Лицензия

MIT License

## 👥 Команда

Разработано командой **Union Coders**

## 🔗 Ссылки

- [GitHub Repository](https://github.com/returnaction/hoka)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [Material-UI](https://mui.com)
- [pgvector](https://github.com/pgvector/pgvector)

---

**Версия:** 1.0.0  
**Последнее обновление:** Октябрь 2025
