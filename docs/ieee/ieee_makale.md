# Modernizing Legacy Banking Systems: The DigiBank Integrated Smart City Architecture

---

**Title — DigiBank: From Legacy Desktop Application to a Service-Oriented Smart City Fintech Platform**

**Author — Ferdi Özaydın, Beykent Üniversitesi**

---

## **Abstract**

Legacy banking software often struggles to meet modern demands for scalability, security, and interoperability. This paper presents the architectural evolution of **DigiBank**, transforming it from a standalone desktop executable (`digibank.exe`) into a robust, service-oriented web platform. The modernized system leverages a **Java 17 backend** for core transaction logic and a **Python Flask frontend** for user interaction, orchestrated via **Docker** containers. The architecture integrates financial services with **Smart City** automation, enabling residents to manage utility payments, home automation, and crypto-asset transactions within a unified interface. Key software engineering principles, including **Adapter**, **Command**, and **Observer** design patterns, are implemented to ensure modularity and extensibility. This study documents the migration strategy, system requirements (SRS), and the resulting hybrid-cloud ready architecture.

---

## **I. INTRODUCTION**

The rapid digitization of urban environments requires financial systems that are not only secure but also deeply integrated with city infrastructure. Traditional banking applications, often developed as monolithic desktop software, lack the flexibility to interact with IoT networks, blockchain ledgers, and web-based utility services.

This work focuses on the modernization of **DigiBank**, originally a reference desktop application (`digibank.exe`) capable of basic local account management. The goal was to re-engineer this legacy tool into a distributed, web-accessible API and Dashboard that serves as a central hub for a "Smart Resident."

The contributions of this paper are:
1.  **Legacy Migration:** A case study on decoupling a desktop monolith into a Client-Server architecture.
2.  **Pattern-Driven Design:** Implementation of standard GoF patterns (Adapter for payments, Observer for notifications) to solve business problems.
3.  **Smart City Integration:** Extending banking logic to cover government bill payments and home automation.

---

## **II. LEGACY SYSTEM OVERVIEW**

The reference system, `digibank.exe`, represents a classic implementation of early 2000s banking software.
*   **Architecture:** Monolithic Desktop Application.
*   **Data Storage:** Local file system or embedded database.
*   **Limitations:** Single-user access, lack of remote connectivity, inability to scale, and absence of modern security standards (MFA/TOTP).

While `digibank.exe` provided a functional baseline for account CRUD (Create, Read, Update, Delete) operations, it was unsuitable for a multi-user, connected Smart City environment.

---

## **III. PROPOSED SYSTEM ARCHITECTURE**

The modernized DigiBank system adopts a **Service-Oriented Architecture (SOA)**, containerized using **Docker** to ensure consistency across development and production environments.

### **A. Component Diagram**

The system allows separation of concerns through the following micro-components:

1.  **Backend Core (Java 17):**
    *   Acts as the central REST API.
    *   Handles authentication (`SHA3-512` hashing + `TOTP`), transaction processing, and database persistence.
    *   Implements the "Hexagonal" or Layered architecture (Controllers, Services, Repositories).
2.  **Frontend GUI (Python Flask):**
    *   A server-side rendered web application serving as the UI.
    *   Consumes the Java API via HTTP requests.
    *   Handles session management and connects to SMTP services for reporting.
3.  **Database (PostgreSQL):**
    *   Relational persistence for Users and Transactions.
    *   Managed via Docker Compose.
4.  **Mail Server (Mailpit):**
    *   A local SMTP server for testing email notifications without external dependencies.

### **B. Design Patterns in Practice**

To ensure code maintainability, several design patterns were pivotal in the rewrite:

*   **Adapter Pattern:** Used in the Payment module to unify interfaces for **Fiat**, **Bitcoin**, and **Ethereum** payments. The system treats all currency types polymorphically.
*   **Command Pattern:** Encapsulates banking operations (Deposit, Withdraw, Transfer) as objects, allowing for audit logging and potential rollback capabilities.
*   **Observer Pattern:** The `NotificationService` observes transaction events. When a high-value transaction occurs, it automatically triggers email alerts or system logs.
*   **Singleton Pattern:** Ensures a single instance of database connections and configuration managers.

---

## **IV. FUNCTIONAL CAPABILITIES**

The system is designed to meet specific Functional Requirements (FR) derived from modern banking standards.

### **A. Core Banking (FR-01, FR-05, FR-08)**
*   **Secure Login:** Users authenticate with username, password, and a Time-based One-Time Password (TOTP).
*   **Transactions:** Supports transfers between accounts and payments to external entities.
*   **Dashboard:** Real-time view of assets and recent activity.

### **B. Smart City Integration (FR-04, FR-11)**
*   **Smart Government:** Residents can view and pay simulated city bills (water, electricity, tax) directly from their banking dashboard.
*   **Home Automation:** The API includes endpoints to control IoT devices (e.g., HVAC thermostat, smart locks), linking financial status with living conditions.

### **C. Administration (FR-07, FR-09, FR-10)**
*   **User Management:** Admins can CRUD users and assign roles.
*   **Analytics:** A dedicated metrics endpoint provides system health snapshots and traffic forecasting.
*   **Reporting:** Capability to export transaction logs to text/CSV format for auditing.

---

## **V. SOFTWARE REQUIREMENTS SPECIFICATION (IEEE SRS)**

### **A. Functional Requirements**
*   **FR-01:** System shall validate users using multi-factor authentication.
*   **FR-02:** System shall support role-based access control (Admin vs. Resident).
*   **FR-03:** Middleware shall authorize API requests via Bearer Tokens.
*   **FR-04:** The Payment Engine shall support pluggable strategies for Fiat and Crypto.
*   **FR-05:** System shall generate detailed transaction logs accessible via API.

### **B. Non-Functional Requirements**
*   **NFR-01 (Security):** Passwords must be salted and hashed (SHA3-512).
*   **NFR-02 (Scalability):** The backend must be stateless to allow horizontal scaling behind a load balancer.
*   **NFR-03 (Deployment):** The entire stack must be deployable via a single orchestration command (`docker compose up`).
*   **NFR-04 (Interoperability):** The API must return standard JSON responses.

---

## **VI. IMPLEMENTATION & RESULTS**

The implementation proved that a legacy concept (`digibank.exe`) could be successfully ported to a web stack without losing business logic integrity. The **Java** backend provides type safety and performance, while **Flask** offers rapid UI development.

**Key Implementation Highlights:**
*   **Directory Watching:** A background service in Java monitors the file system for external transaction files, automatically ingesting them—bridging legacy file-based exchange with modern APIs.
*   **Fault Tolerance:** The repository layer includes fallback mechanisms to In-Memory storage if the PostgreSQL connection fails, ensuring high availability during demos.

---

## **VII. CONCLUSION**

DigiBank demonstrates a practical roadmap for modernizing legacy financial applications. By adopting a containerized, pattern-driven architecture, the system achieves the flexibility required for Smart City integrations while maintaining the rigorous security standards of the financial sector. Future work includes replacing the simulated blockchain adapters with real-world distributed ledger integrations and enhancing the AI-driven forecasting module.

---

## **REFERENCES**

1.  Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.
2.  IEEE Standard 830-1998. *IEEE Recommended Practice for Software Requirements Specifications*.
3.  Richardson, C. (2018). *Microservices Patterns*. Manning Publications.
