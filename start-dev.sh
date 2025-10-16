#!/bin/bash
set -e

echo "🚀 Starting SciBox in Development Mode..."
echo ""
echo "Frontend will be available at: http://localhost:5173"
echo "Backend API at: http://localhost:8080"
echo "Database at: localhost:5000"
echo ""
echo "Hot reload is enabled - changes will apply automatically!"
echo ""

# Start all services in dev mode in detached mode
docker-compose -f docker-compose.dev.yml up -d --build

echo ""
echo "✅ Development environment started!"
echo ""
echo "View logs:"
echo "  docker-compose -f docker-compose.dev.yml logs -f"
echo ""
echo "Stop:"
echo "  ./stop-dev.sh"
echo ""
