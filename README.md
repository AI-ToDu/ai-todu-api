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


### CQRS and REST transformation

Controllers depend on CommandService and QueryService contracts. Domain query records carry workspace identity supplied by the authenticated principal. Internal application services implement those contracts; REST assemblers under interfaces/rest/transform convert aggregates into resources. Persistence assemblers remain separate infrastructure concerns.

Expected Result failures map directly to HTTP ProblemDetail responses, without throwing them again. A command can return either its success resource or ProblemDetail, so its ResponseEntity<?> accurately expresses that HTTP union; OpenAPI explicitly names the concrete schema for each status. Invalid transport input returns 400; domain rejection returns 422 or a specific conflict/not-found status. The HTTP status and body status agree, including security responses.

### Context mapping

Each context's `interfaces/acl` facade is its published application contract. Consuming outbound gateways translate that contract into their own context's model. Logistics consumes Sales; Sales consumes Inventory; Procurement and Returns coordinate Inventory, and Returns consumes Sales. Reporting reads public projections only.

The versioned REST API is the **Open Host Service**. Resources, OpenAPI and [integration message schemas](contracts/integration-messages.v1.schema.json) form the **Published Language**. These are in-process integrations in one deployable application; independent microservices and a message broker are not claimed.

Internal domain events stay inside their originating context. Its application publisher translates them into versioned integration messages. Notifications receives only those public messages.

Low-stock notifications retain the `productId` supplied by the existing v1 integration message. Notifications owns a framework-independent `ProductReference` value object, persisted with a single-attribute JPA converter. Migration V5 adds a nullable `notifications.product_id` so older notifications remain readable. The HTTP resource exposes this optional reference; the web application resolves the SKU/name from its authorized product catalogue. No Inventory domain class or aggregate is imported into Notifications, and no product is inferred for historical records without a reference.

### Transactions, Transactional Outbox Pattern and idempotency

A sale deducts all of its stock, saves its line snapshots and appends integration messages in one database transaction. A failed line rolls everything back. Cancelling a sale restores stock once; dispatching it never deducts stock a second time. Returns cannot exceed sold units and cannot occur during an active dispatch.

A pessimistic workspace lock serializes business commands within a workspace. Database constraints and persistence versions provide further consistency checks. This intentionally coarse lock prioritizes correctness for the academic workload; unrelated workspaces can proceed independently.

The Outbox relay publishes committed messages in batches, retries failures with bounded exponential delay, and records attempts. Notification identity equals the event ID, making repeated delivery idempotent. The relay and local inbox commit together; external broker delivery is a future extension and must retain at-least-once semantics.


This is the **Transactional Outbox Pattern**: business writes and integration_outbox(PENDING) commit together. JpaIntegrationOutbox uses MANDATORY propagation; an uncommitted message is never independently published. OutboxRelay locks due rows in batches of 25; attempts, next_attempt_at and last_error persist retry progress, with a 300-second maximum delay. Its notification write and PUBLISHED update share one local transaction. Notification event-ID uniqueness prevents repeated effects. The integration tests demonstrate rollback, failed delivery, retry and duplicate delivery; they do not restart a complete process or prove external-broker delivery.

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


### Implemented operations

All routes below include `/api/v1`. Public registration/login override bearer security in OpenAPI; protected routes require an active workspace session. JSON requests use Content-Type application/json; Accept-Language is optional. Exact input constraints, resources, examples and applicable errors are available in Swagger and the report's Services Documentation section.

| Verbo | Ruta | Acción / contexto | Éxito | Roles |
| --- | --- | --- | --- | --- |
| POST | `/api/v1/auth/register` | Register a workspace / IAM | 201 | Public |
| POST | `/api/v1/auth/login` | Create a session / IAM | 200 | Public |
| GET | `/api/v1/auth/me` | Read the current identity / IAM | 200 | Any active role |
| POST | `/api/v1/auth/logout` | Revoke the current session / IAM | 204 | Any active role |
| GET | `/api/v1/employees` | List employees / IAM | 200 | ADMIN |
| POST | `/api/v1/employees` | Create an employee / IAM | 201 | ADMIN |
| GET | `/api/v1/products` | List products / Inventory | 200 | Any active role |
| POST | `/api/v1/products` | Register a product / Inventory | 201 | ADMIN, WAREHOUSE |
| PATCH | `/api/v1/products/{id}` | Change conservation status / Inventory | 200 | ADMIN, WAREHOUSE |
| GET | `/api/v1/movements` | List stock movements / Inventory | 200 | ADMIN, WAREHOUSE |
| POST | `/api/v1/movements` | Record a stock movement / Inventory | 201 | ADMIN, WAREHOUSE |
| GET | `/api/v1/suppliers` | List suppliers / Procurement | 200 | ADMIN, WAREHOUSE |
| POST | `/api/v1/suppliers` | Register a supplier / Procurement | 201 | ADMIN, WAREHOUSE |
| GET | `/api/v1/purchases` | List purchase orders / Procurement | 200 | ADMIN, WAREHOUSE |
| POST | `/api/v1/purchases` | Place a purchase order / Procurement | 201 | ADMIN, WAREHOUSE |
| POST | `/api/v1/purchases/{id}/receipts` | Receive a purchase partially or fully / Procurement | 201 | ADMIN, WAREHOUSE |
| GET | `/api/v1/sales` | List sales / Sales | 200 | ADMIN, SALES |
| POST | `/api/v1/sales` | Confirm a sale / Sales | 201 | ADMIN, SALES |
| POST | `/api/v1/sales/{id}/cancel` | Cancel a sale / Sales | 200 | ADMIN, SALES |
| GET | `/api/v1/returns` | List customer returns / Returns | 200 | ADMIN, SALES |
| POST | `/api/v1/returns` | Record a customer return / Returns | 201 | ADMIN, SALES |
| GET | `/api/v1/dispatches` | List dispatch manifests / Logistics | 200 | ADMIN, LOGISTICS |
| POST | `/api/v1/dispatches` | Prepare a dispatch / Logistics | 201 | ADMIN, LOGISTICS |
| PATCH | `/api/v1/dispatches/{id}` | Transition a dispatch / Logistics | 200 | ADMIN, LOGISTICS |
| POST | `/api/v1/dispatches/{id}/incidents` | Record a delivery incident / Logistics | 201 | ADMIN, LOGISTICS |
| GET | `/api/v1/dispatch-candidates` | List eligible sales / Logistics | 200 | ADMIN, LOGISTICS |
| GET | `/api/v1/drivers` | List drivers / Logistics | 200 | ADMIN, LOGISTICS |
| POST | `/api/v1/drivers` | Register a driver / Logistics | 201 | ADMIN, LOGISTICS |
| GET | `/api/v1/vehicles` | List vehicles / Logistics | 200 | ADMIN, LOGISTICS |
| POST | `/api/v1/vehicles` | Register a vehicle / Logistics | 201 | ADMIN, LOGISTICS |
| GET | `/api/v1/logistics/weather` | Read regional Lima weather / Logistics | 200 | ADMIN, LOGISTICS |
| GET | `/api/v1/notifications` | List notifications / Notifications | 200 | Any active role |
| PATCH | `/api/v1/notifications/{id}` | Mark a notification as read / Notifications | 200 | Any active role |
| GET | `/api/v1/metrics` | Read operational metrics / Reporting | 200 | ADMIN |

The OpenAPI contract declares 201 for 13 creation operations, 204 for logout and 200 for the remaining operations. Sales idempotency replay also returns 201. Success schemas and ProblemDetail errors are distinct. No pagination or server-side query filters are implemented.

## Third-party service

The Logistics weather gateway consumes [Open-Meteo](https://open-meteo.com/en/docs) for fixed coordinates in central Lima. It translates the provider response into a local forecast contract, caches results for ten minutes and applies connection/request timeouts. An unavailable provider returns 503; dispatch operations remain independent. No customer address is sent. Conditions are regional planning information, not a route forecast. Open-Meteo attribution appears in the web application.

## Verification

Run `./mvnw verify`. ServicesContractIntegrationTest executes all 34 operations and writes sanitized requests/responses and OpenAPI to target/contract-evidence (ignored by Git). Weather is a deterministic fixture in that contract test; WeatherGatewayTest separately checks the provider adapter. Local tests cover aggregate rules, architectural imports, an HTTP workflow, workspace isolation, JWT logout, roles, partial receipts, returns, concurrent last-unit sales, transactional rollback, sale idempotency and Outbox retries/duplicate delivery.

For PostgreSQL, set `TEST_DATABASE_URL`, `TEST_DATABASE_USER` and `TEST_DATABASE_PASSWORD` to an **isolated test database** before running verification. Tests create fictional records; never point them at a production database. GitHub Actions provisions PostgreSQL 17 and runs the same suite on Java 21.

## Deployment

Build with `docker build -t aitodu-api .`. The multi-stage image runs as a non-root user, reads Render's `PORT`, exposes a health endpoint, and shuts down through the Java process.

The `prod` profile requires `JDBC_DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, and a `JWT_SECRET` containing at least 32 random bytes encoded as Base64. See [.env.example](.env.example); never commit real secrets. Configure `CORS_ORIGINS` explicitly. The development JWT key must not be used in production.

Provision the database and a restricted application role through a separate administrative connection. Ordinary runtime receives only its own database credentials; it has no provisioning or administrative credential.

The free Render database instance has a limited lifetime and shared capacity. Keep exports outside Git and arrange continuity before its expiry. Academic records must use fictional data. No payments, SUNAT documents, AI forecasting or commercial production guarantees are implemented.

## Project delivery

This API supports the AV2 Sprint 3 technical increment. Interview validation, team contribution evidence, Scrum board, videos and academic presentation are tracked in the project report and require real evidence. Development notes and correction checklists remain local, outside this repository.
