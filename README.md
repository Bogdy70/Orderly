# Orderly

Orderly is a personal organization app with a React frontend, Spring Boot backend, PostgreSQL, and Keycloak authentication.

## Run With Docker

Prerequisite: Docker Desktop.

From PowerShell:

```powershell
.\scripts\start-app.ps1
```

The script creates `.env` from `.env.example` if needed, builds the frontend and backend containers, starts PostgreSQL and Keycloak, then waits until the app is ready.

Default URLs:

- Frontend: http://localhost:5173
- Backend Swagger: http://localhost:8080/swagger-ui.html
- Keycloak admin: http://localhost:8081/admin

If `.env` already exists from an older setup, set `FRONTEND_PORT=5173` there before starting the app.

Default dev credentials:

- Keycloak admin: `admin / admin`
- Demo user: `demo / demo`

## Stop

```powershell
.\scripts\stop-app.ps1
```

This stops containers and preserves Docker volumes.

## Reset Local Data

```powershell
.\scripts\reset-app.ps1
```

This deletes local Docker volumes, including PostgreSQL and Keycloak data.

## Optional PgAdmin

```powershell
docker compose -f compose.yaml --profile tools up -d pgadmin
```

PgAdmin defaults to http://localhost:5050 with `admin@orderly.local / admin`.
