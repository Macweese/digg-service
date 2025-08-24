# ---- Build static assets ----
FROM node:20-alpine AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

# ---- Nginx runtime ----
FROM nginx:1.27-alpine
# Copy built assets
COPY --from=build /app/dist/ /usr/share/nginx/html/
# Use repo-root nginx.conf (server block)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]