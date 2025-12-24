# 9. Conference/Journal Report (Task 2.B)

**Title:** DigiBank Integrated Smart-City Automation: A Java + Flask Prototype with MFA, Hybrid Payments, and Operational Tooling

## Abstract

This paper presents a runnable prototype that couples digital banking flows with smart-city automation. A Java 17 backend exposes REST endpoints through an embedded `HttpServer` and enforces SHA3-512 + salt password hashing combined with TOTP-based MFA. Payment execution is modularized with Strategy and Adapter patterns to support fiat and simulated crypto rails (BTC/ETH/Stablecoin). A Flask-based GUI consumes the API, provides admin CRUD screens for users/transactions, and demonstrates reporting features including XLSX/PDF generation and SMTP delivery via a local mail catcher (Mailpit). Data can be persisted to PostgreSQL when enabled through environment variables; otherwise repositories fall back to in-memory storage and file logging.

## I. Introduction

Smart-city services require payments, auditing, and event-driven automation under a unified operational surface. The prototype integrates these concerns in a compact, educational codebase: authentication and authorization, billing and transfers, admin management screens, and automation routines with design-pattern-based separation.

## II. System Design

### A. Runtime Components (as implemented)

1. **Java Backend (ApiServer)**: Embedded HTTP server exposing `/api/*` endpoints (login, user, bills/pay, transfers, home automation, metrics/forecast, export, admin CRUD).
2. **AuthenticationService**: SHA3-512 + per-user salt, constant-time comparison, TOTP verification with skew tolerance, and backoff/temporary lock.
3. **PaymentService**: Strategy pattern for FIAT (`FiatPaymentStrategy`, `CryptoPaymentStrategy`) and Adapter pattern for crypto-bill flows (`BtcAdapter`, `EthAdapter`, `StablecoinAdapter`), logging each operation into `TransactionRepository`.
4. **SmartGovernmentService**: Produces mock government bills and delegates payment execution to PaymentService.
5. **Automation Pipeline**: `CityController` runs routine templates; `SensorSystem` publishes events to observers; `CommandInvoker` executes queued commands (infrastructure and home devices).
6. **Flask GUI (frontend-gui)**: Web UI calling the backend; includes admin screens, export triggers, transaction downloads (XLSX/PDF), and SMTP e-mailing via Mailpit.
7. **PostgreSQL (optional persistence)**: Enabled in Docker Compose; used by `UserRepository` and `TransactionRepository` when `DB_URL/DB_USER/DB_PASS` are provided.
8. **Mailpit**: Local SMTP sink + web UI to inspect outbound mails sent by the GUI.

### B. Key Interaction Flows

1. **Login**: GUI → `POST /api/login` → token returned → GUI session stores token.
2. **Bill Payment**: GUI → `GET /api/bills` → `POST /api/pay` with `payMode=FIAT|BTC|ETH|STABLE` → transaction recorded.
3. **Admin Management**: GUI → `/api/users*` and `/api/transactions*` endpoints to create/update/delete/search.
4. **Reporting**: Backend export writes to `txt/`; GUI creates XLSX/PDF and can email them via SMTP (Mailpit).

## III. Design Patterns

1. **Strategy**: Payment processing policy is swappable (fiat vs crypto balance strategy).
2. **Adapter**: Crypto “rails” for bill payments are abstracted as adapters (BTC/ETH/STABLE simulation).
3. **Observer**: Sensor and file-watcher events are distributed to observers.
4. **Command**: Infrastructure/home actions are queued and executed in order.
5. **Template Method**: Daily routines share a common skeleton with overridable steps.
6. **Singleton**: Central audit logger.

## IV. Deployment & Data

The recommended run mode is Docker Compose with four services: backend, GUI, PostgreSQL, and Mailpit. Persistence is **conditional**: repositories write to PostgreSQL when available, otherwise use in-memory lists/caches and file append logs. Token storage is in-memory.

## V. Security

Implemented security mechanisms are intentionally “prototype-grade”:

- SHA3-512 + salt password hashing
- TOTP verification with window tolerance (demo bypass for DEMO secrets)
- Backoff and temporary account lock after repeated failures
- Role-based authorization for admin endpoints
- Optional HTTPS enforcement behind reverse proxies (`REQUIRE_HTTPS` via `X-Forwarded-Proto`)

## VI. Results

The system demonstrates end-to-end operations: MFA login, bill retrieval and payment with multiple rails, admin CRUD operations, transaction exports, and GUI-based reporting and email delivery through a local SMTP sink.

## VII. Future Work

1. Replace demo token store with expiring tokens and refresh mechanisms.
2. Persist audit logs and add correlation IDs across requests.
3. Introduce rate limiting and strict TLS termination.
4. Evolve smart-city events to a message-bus architecture and add anomaly detection.

## VIII. Conclusion

The prototype provides a compact, runnable baseline showcasing secure authentication, modular payment design, and operational tooling in a smart-city banking context. It is suited for education and as a foundation for production-hardening steps.
