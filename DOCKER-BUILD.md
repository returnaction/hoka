# SciBox Frontend - Docker Build Guide

## Быстрая сборка и запуск

### Способ 1: Автоматическая сборка (рекомендуется)
```bash
./build-frontend.sh
docker-compose up -d
```

### Способ 2: Ручная сборка
```bash
# 1. Собрать frontend локально
cd frontend
npm run build

# 2. Собрать Docker образ
cd ..
docker-compose build frontend

# 3. Запустить все сервисы
docker-compose up -d
```

## Что исправлено

### Проблема
- Docker сборка не работала из-за rolldown-vite (нативные биндинги для musl/glibc)
- npm install падал с ошибкой в Docker контейнере
- Сборка занимала 10+ минут

### Решение
1. **Переключились на стандартный Vite** - убрали rolldown-vite, который требует специфичные native bindings
2. **Двухэтапная сборка**: 
   - Локально: `npm run build` (2-3 секунды с Vite)
   - Docker: копируем готовый `dist` в nginx (3-4 секунды)
3. **Упрощенный Dockerfile.simple** - без сборки внутри контейнера

### Оптимизации
- ✅ Сборка за 5-7 секунд вместо 10+ минут
- ✅ Используется стандартный Vite (быстрее rolldown для небольших проектов)
- ✅ Кеширование слоев Docker
- ✅ Минимальный nginx:alpine образ
- ✅ Gzip compression в nginx
- ✅ .dockerignore для уменьшения build context

## Структура файлов

```
frontend/
├── Dockerfile.simple      # Простой - копирует готовый dist (ИСПОЛЬЗУЕТСЯ)
├── Dockerfile            # Полный - сборка внутри Docker (НЕ работает из-за npm bug)
├── nginx.conf            # Конфигурация nginx с проксями
├── .dockerignore         # Исключения для Docker build context
└── dist/                 # Собранный frontend (создается через npm run build)
```

## Архитектура

```
┌─────────────────────────────────────────────────┐
│           Nginx (Port 3000 → 80)               │
│                                                 │
│  ┌────────────┐  ┌──────────────────────────┐  │
│  │  Frontend  │  │       API Proxies        │  │
│  │  (React)   │  │                          │  │
│  │            │  │  /api/v1/classify →      │  │
│  │  dist/     │  │    backend:8080          │  │
│  │            │  │                          │  │
│  │            │  │  /api/v1/models →        │  │
│  │            │  │    llm.t1v.scibox.tech   │  │
│  └────────────┘  └──────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

## Команды разработки

```bash
# Локальная разработка
cd frontend
npm run dev              # http://localhost:5173

# Production сборка
npm run build           # → dist/

# Пересборка Docker
docker-compose build frontend
docker-compose up -d frontend

# Проверка логов
docker-compose logs -f frontend

# Остановка
docker-compose down
```

## Размер образа
- **Base**: nginx:alpine (~50 MB)
- **Frontend assets**: ~650 KB
- **Total**: ~51 MB

## Performance
- **Build time**: 3-7 секунд
- **Vite build**: 2-3 секунды
- **Docker build**: 3-4 секунды
- **Bundle size**: ~625 KB (196 KB gzipped)

## Troubleshooting

### Если сборка не работает
```bash
# Очистить кеш
docker system prune -a
rm -rf frontend/dist frontend/node_modules

# Пересоздать с нуля
cd frontend
npm install
npm run build
cd ..
docker-compose build --no-cache frontend
```

### Если nginx не стартует
```bash
# Проверить логи
docker-compose logs frontend

# Проверить конфиг nginx
docker exec -it scibox-frontend nginx -t

# Пересоздать контейнер
docker-compose up -d --force-recreate frontend
```

## Production готовность
- ✅ Gzip compression
- ✅ Static asset caching
- ✅ API reverse proxy
- ✅ Health checks
- ✅ Минимальный размер образа
