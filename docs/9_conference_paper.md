# 9. Conference/Journal Report (Task 2.B)

**Title: DigiBank Integrated Smart City Automation System: A Java Prototype with Secure Payments and IoT Automation**

**Abstract**
This paper documents a Java-based prototype that integrates DigiBank’s digital banking flows with Smart City automation. The system exposes REST endpoints via an embedded HttpServer, enforces SHA3-512 + salt password hashing with TOTP-based MFA, executes fiat and simulated crypto payments through Strategy/Adapter patterns, and routes sensor events to city services through an Observer/Command pipeline. We outline the implemented architecture, design patterns, security mechanisms, and operational workflows aligned with the repository’s source code.

---

## **I. INTRODUCTION**

Smart City services need reliable, low-latency payments and responsive event handling. The presented prototype couples DigiBank payments with city automation using a single Java runtime: ApiServer handles HTTP, AuthenticationService validates credentials and TOTP codes, PaymentService executes fiat and crypto payments, and SensorSystem broadcasts incidents to emergency and utility observers. The goal is a demonstrable, auditable flow that mirrors production concerns on a simplified codebase.

---

## **II. SYSTEM DESIGN**

### **A. Core Components (implemented)**

1. **ApiServer**: Embedded HttpServer exposing `/api/login`, `/api/bills`, `/api/pay`, `/api/metrics`, `/api/forecast`, `/api/home/*`, and `/api/export`.
2. **AuthenticationService**: SHA3-512 + per-user salt, TOTP verification (with clock skew tolerance) and temporary lock/backoff after failed attempts.
3. **PaymentService**: Strategy pattern for fiat (FiatPaymentStrategy) and Adapter pattern for crypto (BtcAdapter, EthAdapter, StablecoinAdapter), persisting results to TransactionRepository with AuditLogger traces.
4. **SmartGovernmentService**: Retrieves sample bills and delegates to PaymentService (fiat or crypto) to mark bills as paid.
5. **CityController + SensorSystem + CommandInvoker**: Runs daily routines (LightingRoutine, SecuritySweepRoutine), simulates traffic/fire/water-leak events, and executes queued commands for infrastructure and home devices.
6. **PredictiveAnalyticsService**: Generates demo metrics and traffic/energy forecasts for admin endpoints.
7. **HomeDeviceController / InfrastructureController**: Toggle home devices, set thermostats, and adjust street/traffic lights.

### **B. Interaction Flow (implemented)**

1. Resident/Admin calls `/api/login` with username/password/TOTP. AuthenticationService validates hash + TOTP; on success a random bearer token is issued.
2. Resident fetches bills (`/api/bills`) and pays via `/api/pay` choosing FIAT/BTC/ETH/STABLE. SmartGovernmentService invokes PaymentService, which applies the chosen strategy/adapter and records a Transaction.
3. Admin triggers `/api/export` to write a TXT export; DirectoryWatcherService can notify via EmailNotificationObserver.
4. CityController simulates sensor events; SensorSystem notifies EmergencyService, PublicUtilityService, and BankingNotificationService; CommandInvoker drains queued infrastructure/home commands.

---

## **III. DESIGN PATTERNS (as coded)**

1. **Strategy**: Fiat vs. crypto payment logic (PaymentStrategy, FiatPaymentStrategy, CryptoPaymentStrategy).
2. **Adapter**: Network-specific crypto flows (CryptoAdapter → BTC/ETH/Stablecoin implementations).
3. **Observer**: SensorSystem broadcasting events to emergency, utility, and banking notification observers.
4. **Command**: Queued infrastructure/home actions (TurnOnStreetLightCommand, AdjustTrafficSignalCommand, ToggleHomeDeviceCommand) executed by CommandInvoker.
5. **Template Method**: DailyRoutineTemplate defines routine skeleton; LightingRoutine and SecuritySweepRoutine override steps.
6. **Singleton**: AuditLogger provides centralized audit logging.

---

## **IV. DEPLOYMENT & DATA**

The prototype is packaged with Docker for local runs. Persistence is in-memory repositories plus TXT exports for audit/user lists; no external DB is required in the current build. The API and automation services share the same JVM for simplicity.

---

## **V. SECURITY (implemented)**

1. **Password hashing**: SHA3-512 with per-user salt; constant-time comparison.
2. **MFA**: TOTP with previous/next window tolerance; demo bypass only when secret starts with DEMO and code 000000.
3. **Account protection**: Failure counter with temporary lock and exponential backoff.
4. **Auditability**: AuditLogger writes all auth and payment attempts; TXT export includes admin context.

---

## **VI. RESULTS (prototype scope)**

The system successfully exercises end-to-end flows: MFA login, bill retrieval, fiat and simulated crypto payments, command execution for city/home devices, and observer-based alerting. Metrik ve tahmin uçları örnek veri üretir; kalıcılık ve oranlar demo seviyesindedir.

---

## **VII. FUTURE WORK**

1. Replace crypto rate constants with live feeds and persist balances/transactions to a real datastore.
2. Harden MFA (remove demo bypass), add rate limiting and TLS termination.
3. Split monolith into deployable services (Auth, Payments, IoT) with message queue integration for notifications.
4. Add ML-based anomaly detection on SensorSystem events and payment patterns.

---

## **VIII. CONCLUSION**

The Java prototype operationalizes secure payments, city automation, and observability in a single codebase using well-known design patterns. It provides a runnable baseline for future decomposition into microservices, stronger crypto primitives, and production-grade data persistence.
