@acceptance @card-lifecycle
Feature: Debit Card Issuance and Activation Lifecycle
  As a banking customer
  I want to be issued a debit card linked to my bank account
  So that I can activate it securely and use it for payments

  Scenario: Customer is issued a debit card in INACTIVE state
    Given a customer has an active checking account with initial deposit 1000.00
    When the customer requests a new VISA debit card
    Then the card should be issued successfully
    And the card status should be "INACTIVE"
    And the card number should be masked

  Scenario: Customer activates debit card with valid CVV and sets a 4-digit PIN
    Given an inactive debit card exists for an active bank account
    When the customer activates the card with valid CVV and new PIN "4321"
    Then the card status should change to "ACTIVE"
    And the failed PIN attempts counter should be 0
