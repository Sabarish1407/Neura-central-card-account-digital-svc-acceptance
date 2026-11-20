@acceptance @card-transactions
Feature: Debit Card Transaction Authorization Engine
  As a banking system
  I want to authorize debit card transactions against balances, PIN, and spend limits
  So that funds are safeguarded and overspending is prevented

  Scenario: Successful ATM withdrawal deducts account balance
    Given an active debit card with balance 500.00 and PIN "1234"
    When an ATM withdrawal of 100.00 is requested with PIN "1234"
    Then the transaction should be "APPROVED"
    And an authorization code should be returned
    And the remaining account balance should be 400.00

  Scenario: ATM withdrawal exceeding daily limit is declined
    Given an active debit card with daily ATM limit 500.00 and balance 2000.00
    When an ATM withdrawal of 600.00 is requested with PIN "1234"
    Then the transaction should be "DECLINED" with reason "DAILY_ATM_LIMIT_EXCEEDED"
    And the account balance should remain 2000.00

  Scenario: POS purchase with insufficient balance is declined
    Given an active debit card with balance 30.00 and PIN "1234"
    When a POS purchase of 100.00 is requested with PIN "1234"
    Then the transaction should be "DECLINED" with reason "INSUFFICIENT_FUNDS"
