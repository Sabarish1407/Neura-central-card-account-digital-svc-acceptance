# JIRA User Story: CARD-101 — Digital Banking Debit Card Account Service

**Key:** CARD-101  
**Project:** Neura Central Card Digital Platform  
**Issue Type:** Story  
**Priority:** High  
**Labels:** `neuro-agent`, `digital-banking`, `debit-card`, `true-cd`  
**Status:** Ready for Agent  

---

## 1. Description

As a retail banking customer, I want to manage my debit card lifecycle and execute payment authorizations through a secure digital banking API service. The service must support card issuance, activation with a 4-digit PIN, card freezing/unfreezing, spend controls (ATM, POS, eCommerce, and international toggles), and real-time transaction authorization with strict balance and daily limit enforcement.

---

## 2. Business Acceptance Criteria

### AC 1: Debit Card Issuance
- When an active bank checking or savings account requests a new debit card, the system must generate a 16-digit card number adhering to the Luhn algorithm.
- The initial card status must be set to `INACTIVE`.
- The card expiration date must be 3 years from the date of issuance in `MM/yy` format.
- Default spending limits must be initialized:
  - Daily ATM Withdrawal Limit: **$500.00**
  - Daily POS Purchase Limit: **$2500.00**
  - Daily Online Purchase Limit: **$3000.00**
- International usage toggle must be `false` by default.
- API responses must mask the card number, exposing only the last 4 digits (`**** **** **** 1234`).

### AC 2: Debit Card Activation & PIN Setup
- The cardholder must provide the matching 3-digit CVV to activate the card.
- A 4-digit numeric PIN must be provided and securely hashed using cryptographic one-way hashing before storage.
- Upon successful validation, the card status transitions to `ACTIVE`.
- If an invalid CVV is supplied, the activation must be rejected with HTTP 400 Bad Request.

### AC 3: Transaction Authorization Pipeline
- Transactions must validate that the card exists, is not expired, and status is `ACTIVE`.
- If the card status is `INACTIVE`, `FROZEN`, or `BLOCKED`, the authorization must be declined with the corresponding reason code.
- ATM withdrawals and POS purchases require a matching 4-digit PIN:
  - Each incorrect PIN attempt must increment the failed attempts counter.
  - Upon reaching **3 consecutive failed PIN attempts**, the card must be automatically locked (`BLOCKED`) with reason `PIN_ATTEMPTS_EXCEEDED`.
  - A successful PIN verification must reset the failed PIN attempts counter to 0.
- Daily spending limits must be enforced on cumulative approved transactions for the current calendar day:
  - ATM transactions cannot exceed the daily ATM withdrawal limit ($500.00).
  - POS transactions cannot exceed the daily POS purchase limit ($2500.00).
  - Online transactions cannot exceed the daily online purchase limit ($3000.00).
- If international transactions are attempted while `internationalUsageEnabled == false`, the transaction must be declined with reason `INTERNATIONAL_TRANSACTIONS_DISABLED`.
- The linked bank account must have sufficient available balance (`availableBalance >= transactionAmount`).
- Approved transactions must deduct the amount from both `balance` and `availableBalance`, and generate a 10-character authorization code (`AUTH...`).
- Declined transactions must record a declined transaction entry with the specific `declineReason` without altering account balance.

### AC 4: Spend Controls & Security Management
- Cardholders may freeze or unfreeze their card at any time via the digital banking API.
- Cards reported as `LOST_OR_STOLEN` cannot be reactivated or unfrozen.
- Cardholders can modify their daily spending limits and channel toggles (ATM, POS, eCommerce, Contactless, International).

---

## 3. Testable Atoms & Boundary Matrix

| Atom ID | Target Field | Boundary Condition | Expected Outcome |
|---|---|---|---|
| ATOM-01 | ATM Withdrawal Amount | Amount <= $500.00 daily limit | APPROVED |
| ATOM-02 | ATM Withdrawal Amount | Amount = $500.01 daily limit | DECLINED (`DAILY_ATM_LIMIT_EXCEEDED`) |
| ATOM-03 | Account Balance | Available Balance >= Amount | APPROVED |
| ATOM-04 | Account Balance | Available Balance < Amount | DECLINED (`INSUFFICIENT_FUNDS`) |
| ATOM-05 | Failed PIN Counter | Attempts = 1 or 2 | DECLINED (`INVALID_PIN`), Card remains ACTIVE |
| ATOM-06 | Failed PIN Counter | Attempts = 3 | DECLINED (`PIN_ATTEMPTS_EXCEEDED`), Card transitions to BLOCKED |
| ATOM-07 | International Toggle | Flag = false and isInternational = true | DECLINED (`INTERNATIONAL_TRANSACTIONS_DISABLED`) |
| ATOM-08 | Card Status | Status = FROZEN | DECLINED (`CARD_FROZEN`) |
| ATOM-09 | Card Status | Status = INACTIVE | DECLINED (`CARD_INACTIVE`) |
