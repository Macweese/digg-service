# DIGG Service

A simple REST-based CRUD microservice (Spring Boot backend + Vue 3 frontend)

## Project layout

- Backend (Spring Boot): root project with Maven
- Frontend (Vue 3): `./frontend`
- Containers:
    - Backend image built from Dockerfile at repo root
    - Frontend image built from frontend/Dockerfile
    - Nginx serves the built frontend and can proxy API calls to the backend

## Prerequisites

- Docker and Docker Compose
- Java 17 (for local runs without Docker)
- Node 20 (for local frontend builds without Docker)

## Quick start (Docker Compose)

Build and start both services:

```bash
docker compose up --build
```

Open:
- Frontend: http://localhost:8081
- Backend API: http://localhost:8080
- Health: http://localhost:8080/actuator/health

Stop:

```bash
docker compose down
```

## Backend only (Docker)

Build and run the backend:

```bash
docker build -t digg-service-api:latest .
docker run --rm -p 8080:8080 digg-service-api:latest
```

Test:

```bash
curl http://localhost:8080/digg/user
```

## Frontend only (Docker)

Build and run the frontend:

```bash
docker build -t digg-service-web:latest -f frontend/Dockerfile .
docker run --rm -p 8081:80 digg-service-web:latest
```

## API proxy (optional)

If your frontend calls the API using the `/api` prefix, Nginx will proxy `/api/*` to the backend (see `nginx.conf`). Update your frontend API base URL accordingly, e.g. `/api/digg/user`.

## Development tips

- IntelliJ can run the commands in the fenced code blocks above directly (look for the run icon).
- Use `JAVA_OPTS` to pass custom JVM flags when running the backend container:
  ```bash
  docker run --rm -e JAVA_OPTS="-Xms256m -Xmx512m" -p 8080:8080 digg-service-api:latest
  ```