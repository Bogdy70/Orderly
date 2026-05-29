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

## Database

Create a PostgreSQL database and user:

```sql
CREATE DATABASE orderly;
CREATE USER orderly WITH PASSWORD 'orderly';
GRANT ALL PRIVILEGES ON DATABASE orderly TO orderly;
```

Configure with environment variables if you do not want the defaults:

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/orderly
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

## Seed Data

`V2__seed_demo_data.sql` creates:

- one demo user
- one demo space
- one checklist block with items
- one table block with rows
- one diagram block with nodes and an edge

## Example Requests

Create a user:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"alex@example.com\",\"username\":\"alex\",\"passwordHash\":\"mock-hash\"}"
```

Create a space:

```bash
curl -X POST http://localhost:8080/api/spaces \
  -H "Content-Type: application/json" \
  -d "{\"ownerId\":1,\"name\":\"Personal Plan\",\"description\":\"Notes and next actions\",\"icon\":\"folder\",\"color\":\"#e86d13\"}"
```

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
