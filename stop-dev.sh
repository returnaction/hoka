#!/bin/bash
set -e

echo "🛑 Stopping Development Environment..."

docker-compose -f docker-compose.dev.yml down

echo "✅ Development environment stopped"

