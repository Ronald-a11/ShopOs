# ShopOS

Multi-tenant grocery and supermarket POS for Zimbabwe: a Spring Boot modular
monolith on PostgreSQL, with an offline-capable React + TypeScript client.

The engineering reference is [docs/ShopOS Technical Documentation.pdf](docs/ShopOS%20Technical%20Documentation.pdf).
Read its "Non-negotiable engineering rules" before your first PR.

## Repository layout

```
backend/     Spring Boot app, package zw.miccs.shopos
  src/main/java/zw/miccs/shopos/<module>/
    api/             REST controllers and DTOs (no business logic)
    application/     services, @Transactional boundaries, the module's public interface
    domain/          entities and value objects (no Spring/JPA where avoidable)
    infrastructure/  repositories, JDBC, external clients
  src/main/resources/db/migration/   Flyway migrations (forward-only)
frontend/    React + TypeScript client (Vite)
  src/<feature>/     auth, checkout, catalogue, shifts, inventory, fiscal, offline, backoffice
infra/       docker-compose for local PostgreSQL
docs/        technical documentation and architecture decision records (docs/adr)
.github/     CI/CD workflows, code owners, PR and issue templates
```

Module boundaries are enforced by `ArchitectureTest` in CI: modules call each
other only through `application` services, never another module's `infrastructure`.

## Running locally

Prerequisites: Java 17, Node 24, Docker.

```sh
docker compose -f infra/docker-compose.yml up -d   # PostgreSQL on :5432

cd backend && ./mvnw spring-boot:run               # API on :8080
cd frontend && npm install && npm run dev          # client on :5173, proxies /api to :8080
```

Tests: `./mvnw verify` in `backend` (needs Docker for Testcontainers) and
`npm test` in `frontend`.

## How we work

See [CONTRIBUTING.md](CONTRIBUTING.md).
