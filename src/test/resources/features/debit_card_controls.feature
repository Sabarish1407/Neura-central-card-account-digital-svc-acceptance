@acceptance @card-controls
Feature: Debit Card Spend Controls and Security Toggles
  As a banking customer
  I want to control my card settings such as freezing and toggling international usage
  So that I have full oversight and security on my account

  Scenario: Cardholder freezes debit card and purchase is declined
    Given an active debit card with balance 500.00
    When the cardholder updates the card status to "FROZEN"
    And a POS purchase of 50.00 is attempted
    Then the transaction should be "DECLINED" with reason "CARD_FROZEN"

  Scenario: International transaction is declined when overseas toggle is off
    Given an active debit card with balance 500.00 and international usage disabled
    When an international transaction of 75.00 is requested
    Then the transaction should be "DECLINED" with reason "INTERNATIONAL_TRANSACTIONS_DISABLED"
