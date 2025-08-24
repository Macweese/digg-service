# ---- Build static frontend ----
FROM node:20-alpine AS frontend
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Build backend ----
FROM maven:3.9.8-eclipse-temurin-17 AS backend
WORKDIR /build
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime (single container) ----
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Backend jar
COPY --from=backend /build/target/*.jar /app/app.jar

# Built frontend served by Spring Boot as static content
COPY --from=frontend /frontend/dist/ /app/public/

# Serve static files from /app/public too
ENV SPRING_WEB_RESOURCES_STATIC_LOCATIONS=classpath:/static,classpath:/public,file:/app/public/

# Non-root
RUN useradd -r -u 1001 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]