# Neura Central Card Account Digital Banking Platform

Enterprise Spring Boot multi-repository and multi-module ecosystem implementing complete **Debit Card Digital Banking** functionality. Designed as a reference architecture and target repository suite for the **Nuera Delivery Agent / Central Neuro** autonomous testing, remediation, and verification pipelines.

---

## 1. Ecosystem & Repository Architecture

The platform is structured into five distinct repository components conforming to enterprise banking digital service architecture:

```
Neura-central-card-account-digital-platform/
├── pom.xml                                           # Aggregator Root POM
├── mvnw / mvnw.cmd / .mvn/                           # Maven Wrapper (v3.10.0 / Java 21)
├── docs/                                             # Architecture & Jira requirements
│   └── JIRA_STORY_DEBIT_CARD_DIGITAL_SVC.md         # Ticket CARD-101 with AC & Atoms
│
├── Neura-central-card-account-digital-model/         # Shared Contracts, DTOs & Domain Models
│   ├── src/main/java/com/nuera/banking/card/model/
│   │   ├── dto/                                      # Request / Response DTOs & Card Controls
│   │   └── enums/                                    # Status, Type, Channel & Decline Enums
│   └── pom.xml
│
├── Neura-central-card-account-digital-svc/           # Core Spring Boot REST Microservice
│   ├── src/main/java/com/nuera/banking/card/
│   │   ├── controller/                               # Cards, Transactions, Accounts REST APIs
│   │   ├── service/                                  # Authorization Engine & Business Logic
│   │   ├── repository/                               # Spring Data JPA H2 Persistence
│   │   ├── entity/                                   # Database Entities
│   │   ├── exception/                                # Global Exception Handler & Standard Error
│   │   └── util/                                     # Luhn Checksum, Masking, Secret Hashing
│   ├── src/main/resources/application.yml
│   └── pom.xml
│
├── Neura-central-card-account-digital-svc-acceptance/# Cucumber BDD Acceptance Tests
│   ├── src/test/resources/features/                  # Gherkin Scenarios for Issuance, Auth, Controls
│   ├── src/test/java/com/nuera/banking/card/acceptance/
│   └── pom.xml
│
├── Neura-central-card-account-digital-svc-aft/       # Automated Functional Tests (AFT)
│   ├── src/test/java/com/nuera/banking/card/aft/     # Live HTTP Server & Component Tests
│   └── pom.xml
│
└── Neura-central-card-account-digital-svc-end2end/   # End-to-End Scenario Tests (REST Assured)
    ├── src/test/java/com/nuera/banking/card/end2end/ # Full User & Merchant Journeys
    └── pom.xml
```

---

## 2. Core Debit Card Banking Capabilities

| Capability | Description |
|---|---|
| **Debit Card Issuance** | Luhn-compliant 16-digit card number generation, 3-year expiration, CVV generation, default limits. |
| **Card Activation & PIN Setup** | Cryptographic verification of CVV, 4-digit PIN setup via SHA-256 one-way hashing with zero plaintext storage. |
| **Real-time Authorization Engine** | Multi-channel authorization (ATM, POS, eCommerce) with PIN verification, daily spend limits, balance checks, and atomic balance deduction. |
| **Fraud & Security Guardrails** | Automatic card blocking after 3 consecutive failed PIN attempts; instant card freeze/unfreeze; permanent lost/stolen blocking. |
| **Channel & Spend Controls** | Granular controls for Daily ATM Limit ($500), POS Limit ($2,500), Online Limit ($3,000), and toggles for international, contactless, ATM, and online transactions. |
| **Audit & Transaction History** | Complete approved/declined transaction ledger with reference numbers, authorization codes, and standardized decline reason codes. |

---

## 3. Technology Stack

- **Runtime:** Java 21 LTS (OpenJDK 21.0.12)
- **Framework:** Spring Boot 3.4.3
- **Data Persistence:** Spring Data JPA with H2 in-memory embedded DB (zero external setup required)
- **Validation:** Jakarta Bean Validation (JSR-380)
- **Documentation:** OpenAPI 3 / Swagger (`/swagger-ui.html`)
- **BDD Testing:** Cucumber JVM 7.20.1
- **E2E Testing:** REST Assured 5.5.0
- **Build System:** Apache Maven 3.10.0 with bundled Maven Wrapper (`mvnw`)

---

## 4. API Endpoints Reference

### Cards API (`/api/v1/cards`)
- `POST /api/v1/cards/issue` — Issue new debit card for an active account
- `POST /api/v1/cards/{cardId}/activate` — Activate debit card with CVV and set initial PIN
- `GET /api/v1/cards/{cardId}` — Get masked card details (`**** **** **** 1234`)
- `GET /api/v1/cards/account/{accountId}` — List all cards for a bank account
- `PUT /api/v1/cards/{cardId}/status` — Update card status (`ACTIVE`, `FROZEN`, `BLOCKED`, `LOST_OR_STOLEN`)
- `PUT /api/v1/cards/{cardId}/pin` — Change PIN with current PIN verification
- `PUT /api/v1/cards/{cardId}/controls` — Update daily spending limits and channel toggles
- `POST /api/v1/cards/{cardId}/reset-pin-attempts` — Reset failed PIN counter and unblock card

### Transactions API (`/api/v1/transactions`)
- `POST /api/v1/transactions/authorize` — Authorize debit card payment across channels (ATM, POS, eCommerce)
- `GET /api/v1/transactions/{transactionId}` — Get transaction record by ID
- `GET /api/v1/transactions/card/{cardId}` — Get full transaction history for a card

### Accounts API (`/api/v1/accounts`)
- `POST /api/v1/accounts` — Open bank checking / savings account
- `GET /api/v1/accounts/{accountId}` — Get account details and balance
- `POST /api/v1/accounts/{accountId}/deposit` — Deposit funds into account

---

## 5. Building and Running Tests

### Build Entire Platform:
```bash
./mvnw clean install
```

### Run Module-Specific Test Suites:
```bash
# Unit & Slice Tests
cd Neura-central-card-account-digital-svc
./mvnw test

# Cucumber BDD Acceptance Tests
cd ../Neura-central-card-account-digital-svc-acceptance
./mvnw test

# Automated Functional Tests (AFT)
cd ../Neura-central-card-account-digital-svc-aft
./mvnw test

# End-to-End Tests (REST Assured)
cd ../Neura-central-card-account-digital-svc-end2end
./mvnw test
```

### Launch Application Server:
```bash
cd Neura-central-card-account-digital-svc
./mvnw spring-boot:run
```
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Endpoint: `http://localhost:8080/actuator/health`
- H2 Console: `http://localhost:8080/h2-console`
