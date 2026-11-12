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
mobile/      React Native + Expo app (Expo Router), runs in Expo Go
  src/app/           screens; every file is a route, (auth)/ holds sign-in screens
  src/components/    shared UI; src/theme.ts mirrors the web colour tokens
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
cd mobile && npm install && npx expo start         # scan the QR code with Expo Go
```

Mobile: install **Expo Go** from the Play Store or App Store, keep the phone on
the same Wi-Fi as your computer, and scan the QR code. If the network blocks it,
use `npx expo start --tunnel`. Add packages with `npx expo install <pkg>`, not
`npm install`, so versions match the Expo SDK; packages with custom native code
won't run in Expo Go.

Tests: `./mvnw verify` in `backend` (needs Docker for Testcontainers),
`npm test` in `frontend` and `npm run typecheck` in `mobile`.

## How we work

See [CONTRIBUTING.md](CONTRIBUTING.md).
