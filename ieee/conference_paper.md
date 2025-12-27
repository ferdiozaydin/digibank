# Conference/Journal Report

**Title:** DigiBank: Architecture of a Service-Oriented Smart City Fintech Platform

## Abstract

This paper details the technical implementation of **DigiBank**, a modernized banking platform designed to operate within a Smart City ecosystem. Moving away from a monolithic desktop legacy (`digibank.exe`), the new system employs a **Java 17** backend for core transaction processing and a **Python Flask** frontend for user interaction, all orchestrated via **Docker**. The platform demonstrates the integration of traditional financial services with smart city utilities (IoT control, bill payments) and supports hybrid payment rails (Fiat and Crypto). The architecture significantly utilizes **Design Patterns** such as Adapter, Command, and Observer to achieve modularity and scalability.

## I. Introduction

The objective of this project was to re-engineer a legacy single-user banking application into a distributed, multi-user system capable of serving the needs of a modern "Smart Resident." This report outlines the system design, the chosen technology stack, and the software engineering principles applied during the modernization process.

## II. System Design & Architecture

### A. Core Components
The system is built on a Microservice-like architecture, containerized for consistent deployment:

1.  **Backend Core (Java):**
    *   Exposes a RESTful API.
    *   Handles business logic including Authentication (MFA), Account Management, and Transactions.
    *   Implements a Hexagonal Architecture to isolate core domain logic from external dependencies.

2.  **Frontend GUI (Flask):**
    *   Serves as the presentation layer.
    *   Communicates with the backend via HTTP.
    *   Provides specialized dashboards for Residents (banking/home control) and Admins (system monitoring).

3.  **Infrastructure:**
    *   **PostgreSQL:** Relational database for persistent storage.
    *   **Mailpit:** SMTP service for testing email notifications.
    *   **Docker Compose:** Orchestration tool to manage the lifecycle of all services.

### B. Smart City Features
DigiBank extends beyond traditional banking:
*   **Smart Government:** A module that simulates city utility bills (water, electricity) and allows payment via the banking interface.
*   **Home Automation:** A dedicated API endpoint allows users to control IoT devices (e.g., smart lights, thermostats) directly from their banking dashboard.

## III. Applied Design Patterns

The robustness of the codebase is achieved through the rigorous application of GoF Design Patterns:

1.  **Adapter Pattern:**
    *   *Problem:* The need to support multiple payment methods (Fiat, Bitcoin, Ethereum) with different underlying APIs.
    *   *Solution:* A common `PaymentAdapter` interface allows the system to treat all payment types uniformly, making the addition of new currencies effortless.

2.  **Observer Pattern:**
    *   *Problem:* Critical events (e.g., high-value transfers) need to trigger multiple reactions (Email alert, Audit log) without tightly coupling components.
    *   *Solution:* A `NotificationService` observes transaction events and notifies registered listeners automatically.

3.  **Command Pattern:**
    *   *Problem:* Banking operations need to be transactional and potentially reversible.
    *   *Solution:* Each operation (Deposit, Withdraw) is encapsulated as a Command object, standardizing execution and logging.

## IV. Security Implementation

Despite being a prototype, the system enforces strict security standards:
*   **Passwords:** Salted and hashed using `SHA3-512`.
*   **MFA:** Integration of Time-based One-Time Passwords (TOTP) for login.
*   **Authorization:** Role-Based Access Control (RBAC) helps differentiate between Admin and Resident capabilities.

## V. Results and Conclusion

The DigiBank project successfully demonstrates the feasibility of modernizing legacy software into a cloud-native, service-oriented platform. The resulting system is scalable, secure, and ready for integration into a larger Smart City network. Future iterations will focus on replacing simulated adapters with real-world blockchain and IoT interfaces.

## VI. Future Work

1.  **Real-Blockchain Integration:** Replacing the simulated crypto adapters with actual Web3 libraries.
2.  **Live IoT Connection:** Connecting the Home Automation module to physical smart devices via MQTT.
3.  **AI Analytics:** Implementing the planned machine learning module to forecast transaction trends and detect anomalies.
