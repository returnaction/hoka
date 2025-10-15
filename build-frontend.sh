#!/bin/bash
set -e

echo "🚀 Building SciBox Frontend..."

# Navigate to frontend directory
cd "$(dirname "$0")/frontend"

# Build frontend locally (fast with Vite)
echo "📦 Building frontend with Vite..."
npm run build

# Build Docker image
echo "🐳 Building Docker image..."
cd ..
docker-compose build frontend

echo "✅ Frontend build complete!"
echo "Run: docker-compose up -d to start all services"
