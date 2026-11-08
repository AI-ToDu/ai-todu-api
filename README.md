# AI-ToDu Platform API

[![Verify API](https://github.com/AI-ToDu/ai-todu-api/actions/workflows/verify.yml/badge.svg)](https://github.com/AI-ToDu/ai-todu-api/actions/workflows/verify.yml)

Transactional backend for **AI-ToDu**, the IA-INNOVATION academic project. Java 21, Spring Boot 4.1.2, Spring Security, JPA/Hibernate, Flyway and PostgreSQL 17.

[Web application](https://ai-todu.github.io/ai-todu-webapp/) · [Landing page](https://ai-todu.github.io/ai-todu-platform/) · [Project report](https://github.com/AI-ToDu/ai-todu-report)

## Run locally

Install JDK 21 or later, then run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). The default `dev` profile uses a local H2 file in `data/`; it is for development only.

- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Health: `http://localhost:8080/actuator/health`

Register a fictional workspace with `POST /api/v1/auth/register`, then sign in at `POST /api/v1/auth/login`. Registration accepts `name`, `company`, `email`, and `password` (8 characters minimum, 72 UTF-8 bytes maximum). No public seed account is installed.

## Architecture and domain boundaries

The application is a **modular monolith**. Each bounded context has its own domain, application, infrastructure and interfaces packages:

| Context | Owns |
| --- | --- |
| IAM | Workspaces, accounts, roles and revocable sessions |
| Inventory | InventoryItem, SKU, stock movements and conservation |
| Procurement | Suppliers, purchase orders and partial receipts |
| Sales | Confirmed sales, immutable price/cost snapshots and return counts |
| Returns | Return disposition and recorded losses |
| Logistics | DispatchManifest, drivers, vehicles, incidents and delivery evidence |
| Notifications | An inbox of integration messages |
| Reporting | Read projections assembled through public context facades |

Domain classes import neither Spring nor JPA, REST resources, nor another context's domain. Aggregates enforce their transitions and expose immutable collections. Commands belong to the domain. Application handlers coordinate repositories and transactions. REST **resources** describe HTTP contracts.

Domain objects and persistence entities are separate. Explicit persistence assemblers reconstitute aggregates. Single-attribute values such as SKU, Quantity and Money use JPA converters; multi-attribute persistence values such as delivery evidence and order lines use embeddables. Domain value objects carry no persistence annotations.

### Context mapping

Each context's `interfaces/acl` facade is its published application contract. Consuming outbound gateways translate that contract into their own context's model. Logistics consumes Sales; Sales consumes Inventory; Procurement and Returns coordinate Inventory, and Returns consumes Sales. Reporting reads public projections only.

The versioned REST API is the **Open Host Service**. Resources, OpenAPI and [integration message schemas](contracts/integration-messages.v1.schema.json) form the **Published Language**. These are in-process integrations in one deployable application; independent microservices and a message broker are not claimed.

Internal domain events stay inside their originating context. Its application publisher translates them into versioned integration messages. Notifications receives only those public messages.

### Transactions, Outbox and idempotency

A sale deducts all of its stock, saves its line snapshots and appends integration messages in one database transaction. A failed line rolls everything back. Cancelling a sale restores stock once; dispatching it never deducts stock a second time. Returns cannot exceed sold units and cannot occur during an active dispatch.

A pessimistic workspace lock serializes business commands within a workspace. Database constraints and persistence versions provide further consistency checks. This intentionally coarse lock prioritizes correctness for the academic workload; unrelated workspaces can proceed independently.

The Outbox relay publishes committed messages in batches, retries failures with bounded exponential delay, and records attempts. Notification identity equals the event ID, making repeated delivery idempotent. The relay and local inbox commit together; external broker delivery is a future extension and must retain at-least-once semantics.

`POST /sales` accepts an optional `Idempotency-Key`. A repeated key with the same product quantities returns the recorded sale without deducting stock again; a conflicting payload returns 409. Other create commands do not yet provide a generalized idempotency key.

## API and security

Collection endpoints return `{"items":[]}`. Commands use resources; errors return Problem Details with a stable `code`. `Accept-Language: en-US` and `es-419` select error language.

| Capability | Paths | Roles |
| --- | --- | --- |
| Identity | `/auth/register`, `/auth/login`, `/auth/me`, `/auth/logout` | Public registration/login; authenticated session |
| Inventory | `/products`, `/movements` | Read products: all; commands/history: ADMIN, WAREHOUSE |
| Purchasing | `/suppliers`, `/purchases`, `/purchases/{id}/receipts` | ADMIN, WAREHOUSE |
| Sales | `/sales`, `/sales/{id}/cancel` | ADMIN, SALES |
| Returns | `/returns` | ADMIN, SALES |
| Dispatches | `/dispatches`, `/dispatch-candidates`, `/drivers`, `/vehicles` | ADMIN, LOGISTICS |
| Weather | `/logistics/weather` | ADMIN, LOGISTICS |
| Accounts | `/employees` | ADMIN |
| Inbox | `/notifications` | Authenticated workspace members |
| Metrics | `/metrics` | ADMIN |

Passwords use BCrypt. Signed HS256 JWTs expire after eight hours; the server stores only a SHA-256 token digest for immediate logout revocation and checks the current account on every request. Roles and workspace IDs come from the authenticated account, never from request bodies. All repository reads are workspace scoped. Employee creation assigns an initial password through the administrator; email invitations and password recovery are future work.

## Third-party service

The Logistics weather gateway consumes [Open-Meteo](https://open-meteo.com/en/docs) for fixed coordinates in central Lima. It translates the provider response into a local forecast contract, caches results for ten minutes and applies connection/request timeouts. An unavailable provider returns 503; dispatch operations remain independent. No customer address is sent. Conditions are regional planning information, not a route forecast. Open-Meteo attribution appears in the web application.

## Verification

Run `./mvnw verify`. Local tests cover aggregate rules, architectural imports, an HTTP workflow, workspace isolation, JWT logout, roles, partial receipts, returns, concurrent last-unit sales, transactional rollback, sale idempotency and Outbox retries/duplicate delivery.

For PostgreSQL, set `TEST_DATABASE_URL`, `TEST_DATABASE_USER` and `TEST_DATABASE_PASSWORD` to an **isolated test database** before running verification. Tests create fictional records; never point them at a production database. GitHub Actions provisions PostgreSQL 17 and runs the same suite on Java 21.

## Deployment

Build with `docker build -t aitodu-api .`. The multi-stage image runs as a non-root user, reads Render's `PORT`, exposes a health endpoint, and shuts down through the Java process.

The `prod` profile requires `JDBC_DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, and a `JWT_SECRET` containing at least 32 random bytes encoded as Base64. See [.env.example](.env.example); never commit real secrets. Configure `CORS_ORIGINS` explicitly. The development JWT key must not be used in production.

Provision the database and a restricted application role through a separate administrative connection. Ordinary runtime receives only its own database credentials; it has no provisioning or administrative credential.

The free Render database instance has a limited lifetime and shared capacity. Keep exports outside Git and arrange continuity before its expiry. Academic records must use fictional data. No payments, SUNAT documents, AI forecasting or commercial production guarantees are implemented.

## Project delivery

This API supports the AV2 Sprint 3 technical increment. Interview validation, team contribution evidence, Scrum board, videos and academic presentation are tracked in the project report and require real evidence. Development notes and correction checklists remain local, outside this repository.
