# Development Mode Guide

## 🚀 Quick Start - Dev Mode with Hot Reload

```bash
# Start development environment
./start-dev.sh

# Stop development environment
./stop-dev.sh
```

## 📦 What's Included

### Development Environment (`docker-compose.dev.yml`)
- **Frontend**: Vite dev server with hot reload (http://localhost:5173)
- **Backend**: Spring Boot with live reload (http://localhost:8080)
- **Database**: PostgreSQL with pgvector (localhost:5000)

### Features
- ✅ **Hot Module Replacement (HMR)** - Frontend changes apply instantly
- ✅ **Live Reload** - Edit TypeScript/React files, see changes immediately
- ✅ **Volume Mounts** - Source code mounted from host to container
- ✅ **No rebuild needed** - Just save and refresh browser
- ✅ **Fast iteration** - No `npm run build` or Docker rebuild

## 🔧 How It Works

### Frontend (Vite Dev Server)
```yaml
volumes:
  - ./frontend/src:/app/src  # Mount source code
  - /app/node_modules         # Exclude node_modules
```

**Vite config** (`vite.config.ts`):
- `host: '0.0.0.0'` - Allow Docker container access
- `watch.usePolling: true` - Enable file watching in Docker volumes
- `hmr.clientPort: 5173` - Hot reload configuration

### Workflow
1. Edit file: `frontend/src/pages/OperatorDesk/ChatPanel/ChatPanel.tsx`
2. Save (Ctrl+S)
3. Browser auto-refreshes instantly! ⚡

## 📋 Commands

### Start Development
```bash
# Full stack with hot reload
./start-dev.sh

# Or manually
docker-compose -f docker-compose.dev.yml up --build
```

### Stop Development
```bash
./stop-dev.sh

# Or manually
docker-compose -f docker-compose.dev.yml down
```

### View Logs
```bash
# All services
docker-compose -f docker-compose.dev.yml logs -f

# Frontend only
docker-compose -f docker-compose.dev.yml logs -f frontend

# Backend only
docker-compose -f docker-compose.dev.yml logs -f backend
```

### Rebuild Single Service
```bash
# Rebuild frontend only
docker-compose -f docker-compose.dev.yml up -d --build frontend

# Rebuild backend only
docker-compose -f docker-compose.dev.yml up -d --build backend
```

## 🆚 Dev vs Production

| Feature | Development | Production |
|---------|-------------|------------|
| **Port** | 5173 (Vite) | 3000 (Nginx) |
| **Hot Reload** | ✅ Yes | ❌ No |
| **Build Time** | Instant | 2-3 sec |
| **File Changes** | Auto-apply | Need rebuild |
| **Source Maps** | ✅ Yes | ❌ No |
| **Optimized** | ❌ No | ✅ Yes (minified) |

## 🐛 Troubleshooting

### Changes not applying?
```bash
# Check if volumes are mounted
docker-compose -f docker-compose.dev.yml exec frontend ls -la /app/src

# Restart frontend container
docker-compose -f docker-compose.dev.yml restart frontend

# Hard refresh browser
Ctrl + Shift + R (or Cmd + Shift + R on Mac)
```

### Port conflicts?
```bash
# Check if port 5173 is in use
lsof -i :5173

# Kill process using port
kill -9 $(lsof -t -i :5173)
```

### Performance issues?
```bash
# Reduce polling interval in vite.config.ts
server: {
  watch: {
    usePolling: true,
    interval: 1000  # Increase interval (ms)
  }
}
```

## 📁 File Structure

```
scibox-frontend/
├── docker-compose.dev.yml    # Dev environment config
├── docker-compose.yml         # Production config
├── start-dev.sh              # Start dev mode
├── stop-dev.sh               # Stop dev mode
└── frontend/
    ├── Dockerfile.dev        # Dev Docker image
    ├── Dockerfile.simple     # Production image
    ├── vite.config.ts        # Vite with Docker config
    └── src/                  # Source code (mounted)
```

## 💡 Tips

1. **Use Dev Mode for development** - Instant feedback
2. **Use Production Mode for testing** - Final build verification
3. **Browser DevTools** - React DevTools work perfectly
4. **Network Tab** - Monitor API calls to backend
5. **Console** - Check for TypeScript errors

## 🎯 Common Development Tasks

### Add new component
```bash
# 1. Create file
touch frontend/src/components/MyComponent.tsx

# 2. Edit file
# ... your code ...

# 3. Import in parent
# Changes apply automatically!
```

### Update styles
```bash
# 1. Edit MUI styles in component
sx={{ color: 'primary.main' }}

# 2. Save file
# 3. Browser updates instantly! ⚡
```

### Test API endpoint
```bash
# Backend logs
docker-compose -f docker-compose.dev.yml logs -f backend

# Make API call from frontend
# Watch logs in real-time
```

## 🚀 Production Deployment

When ready for production:
```bash
# Stop dev environment
./stop-dev.sh

# Build and start production
./build-frontend.sh
docker-compose up -d
```

---

**Happy coding!** 🎉
