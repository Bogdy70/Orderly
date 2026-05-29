# Orderly backend

Spring Boot API for Orderly spaces, blocks, checklist items, table rows and diagrams.

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- PostgreSQL
- Flyway migrations
- Springdoc OpenAPI / Swagger UI
- Spring Security OAuth2 resource server
- Keycloak for local authentication

## Database

The easiest local setup is Docker Compose from the repository root:

```bash
docker compose up -d postgres
```

If your Docker install exposes Compose as the standalone command, use:

```bash
docker-compose up -d postgres
```

This starts PostgreSQL with the same defaults used by `application.yml`:

```text
host: localhost
port: 5433
database: orderly
username: orderly
password: orderly
```

The container's internal PostgreSQL port is still `5432`, but it is published on host port `5433` by default to avoid conflicts with a locally installed PostgreSQL service:

```text
DATABASE_URL=jdbc:postgresql://localhost:5433/orderly
```

To also start pgAdmin:

```bash
docker compose --profile tools up -d
```

or:

```bash
docker-compose --profile tools up -d
```

Then open:

```text
http://localhost:5050
```

Default pgAdmin login:

```text
email: admin@orderly.local
password: admin
```

Inside pgAdmin, register a server with host `postgres`, database `orderly`, username `orderly`, and password `orderly`.

Stop the containers:

```bash
docker compose down
```

or:

```bash
docker-compose down
```

Remove the database volume and start fresh:

```bash
docker compose down -v
```

or:

```bash
docker-compose down -v
```

If you prefer a manually installed PostgreSQL server, create a PostgreSQL database and user:

```sql
CREATE DATABASE orderly;
CREATE USER orderly WITH PASSWORD 'orderly';
GRANT ALL PRIVILEGES ON DATABASE orderly TO orderly;
```

Configure with environment variables if you do not want the defaults:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5433/orderly
DATABASE_USERNAME=orderly
DATABASE_PASSWORD=orderly
PORT=8080
```

## Run

```bash
mvn spring-boot:run
```

Flyway runs automatically on startup. Migrations are in `src/main/resources/db/migration`.

## Build and test

```bash
mvn test
```

Build the jar:

```bash
mvn clean package
```

Run the built jar:

```bash
java -jar target/orderly-backend-0.1.0.jar
```

## Swagger

After starting the backend, open:

```text
http://localhost:8080/swagger-ui.html
```

The OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

## Keycloak

Start Keycloak and its database from the repository root:

```bash
docker-compose --profile auth up -d keycloak
```

This imports the local realm from `backend/keycloak/orderly-realm.json`.

Keycloak admin console:

```text
http://localhost:8081
```

Default admin login:

```text
username: admin
password: admin
```

Imported realm:

```text
orderly
```

Frontend public client:

```text
orderly-frontend
```

Demo user:

```text
username: demo
password: demo
email: demo@orderly.local
email verified: true
```

The backend validates JWTs with:

```text
KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/orderly
```

`/api/**` endpoints require a Keycloak access token. Swagger and OpenAPI docs remain public.

Swagger is configured for Keycloak OAuth2 login with PKCE. Open Swagger UI, click `Authorize`, use client `orderly-frontend`, and log in with a Keycloak account. After that, Swagger sends the token for API requests automatically.

Swagger now uses a fixed local OAuth callback URL: `http://localhost:8080/swagger-ui/oauth2-redirect.html`. That keeps the redirect URI stable and avoids host-based mismatches during login.

```text
http://localhost:8080/swagger-ui/index.html
```

The backend also requires `email_verified=true` and a non-empty `email` claim in the JWT. Disable this only for local debugging:

```bash
REQUIRE_VERIFIED_EMAIL=false
```

Get a local access token with the demo user:

```bash
curl -X POST "http://localhost:8081/realms/orderly/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=orderly-frontend" \
  -d "grant_type=password" \
  -d "username=demo" \
  -d "password=demo"
```

Use the returned `access_token`:

```bash
curl http://localhost:8080/api/spaces \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Create a Keycloak account through the backend:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"alex@example.com\",\"username\":\"alex\",\"password\":\"change-me\"}"
```

## Seed Data

`V2__seed_demo_data.sql` creates:

- one demo user
- one demo space
- one checklist block with items
- one table block with rows
- one diagram block with nodes and an edge

## Example Requests

Connect the current Keycloak account to a local Orderly user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d "{\"email\":\"alex@example.com\",\"username\":\"alex\"}"
```

The posted `email` must match the verified email claim in the authenticated Keycloak token. The backend stores the Keycloak subject as `keycloak_id`; it does not store passwords or password hashes.

Read the connected local user:

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

This returns `404` until the authenticated Keycloak account is connected with `POST /api/users`.

Create a space:

```bash
curl -X POST http://localhost:8080/api/spaces \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d "{\"name\":\"Personal Plan\",\"description\":\"Notes and next actions\",\"icon\":\"folder\",\"color\":\"#e86d13\"}"
```

`ownerId` is assigned from the authenticated Keycloak user. You should not pass it in Swagger.

Create a checklist block:

```bash
curl -X POST http://localhost:8080/api/spaces/1/blocks \
  -H "Content-Type: application/json" \
  -d "{\"type\":\"checklist\",\"title\":\"Today\",\"position\":1}"
```

Add a checklist item:

```bash
curl -X POST http://localhost:8080/api/blocks/1/checklist-items \
  -H "Content-Type: application/json" \
  -d "{\"text\":\"Review priorities\",\"done\":false,\"position\":1}"
```

Read a full space:

```bash
curl http://localhost:8080/api/spaces/1/full
```

Convert a block:

```bash
curl -X POST http://localhost:8080/api/blocks/1/convert/table
```
