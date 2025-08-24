# ---- Build stage (backend) ----
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage (backend) ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Optional: install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Non-root user
RUN useradd -r -u 1001 appuser
COPY --from=build /app/target/*.jar /app/app.jar
RUN chown appuser:appuser /app/app.jar
USER appuser

EXPOSE 8080

# Health check (Spring Boot actuator)
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]