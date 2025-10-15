# Build stage
FROM node:20-slim AS builder

WORKDIR /app

COPY package*.json ./

# Workaround for npm bug in Docker - use --force to continue on errors
RUN npm install --force --loglevel=error || (cat /root/.npm/_logs/*.log && exit 1)

COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
