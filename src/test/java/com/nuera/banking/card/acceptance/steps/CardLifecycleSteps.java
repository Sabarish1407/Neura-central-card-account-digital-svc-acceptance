package com.nuera.banking.card.acceptance.steps;

import com.nuera.banking.card.entity.AccountEntity;
import com.nuera.banking.card.entity.CardEntity;
import com.nuera.banking.card.model.dto.*;
import com.nuera.banking.card.model.enums.*;
import com.nuera.banking.card.repository.AccountRepository;
import com.nuera.banking.card.repository.CardRepository;
import com.nuera.banking.card.service.AccountService;
import com.nuera.banking.card.service.CardService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class CardLifecycleSteps {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CardService cardService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    private AccountResponse currentAccount;
    private CardResponse currentCard;
    private String issuedCardCvv;
    private String rawCardNumber;

    @Given("a customer has an active checking account with initial deposit {double}")
    public void aCustomerHasAnActiveCheckingAccount(double initialDeposit) {
        AccountCreateRequest req = new AccountCreateRequest(
                "cust-acceptance", "Alice Acceptance", AccountType.CHECKING,
                BigDecimal.valueOf(initialDeposit), "USD"
        );
        currentAccount = accountService.createAccount(req);
    }

    @When("the customer requests a new VISA debit card")
    public void theCustomerRequestsANewVisaDebitCard() {
        CardIssuanceRequest req = new CardIssuanceRequest(
                currentAccount.getAccountId(), "ALICE ACCEPTANCE",
                CardType.DEBIT, CardNetwork.VISA, "USD"
        );
        currentCard = cardService.issueDebitCard(req);
    }

    @Then("the card should be issued successfully")
    public void theCardShouldBeIssuedSuccessfully() {
        assertThat(currentCard).isNotNull();
        assertThat(currentCard.getCardId()).isNotBlank();
    }

    @And("the card status should be {string}")
    public void theCardStatusShouldBe(String expectedStatus) {
        CardResponse fetched = cardService.getCardById(currentCard.getCardId());
        assertThat(fetched.getCardStatus().name()).isEqualTo(expectedStatus);
    }

    @And("the card number should be masked")
    public void theCardNumberShouldBeMasked() {
        assertThat(currentCard.getMaskedCardNumber()).startsWith("**** **** **** ");
    }

    @Given("an inactive debit card exists for an active bank account")
    public void anInactiveDebitCardExistsForAnActiveBankAccount() {
        aCustomerHasAnActiveCheckingAccount(500.00);
        theCustomerRequestsANewVisaDebitCard();

        // Retrieve raw CVV directly from entity in test context
        CardEntity card = cardRepository.findById(currentCard.getCardId()).orElseThrow();
        // For test activation, we can set known CVV hash
        card.setCvvHash(com.nuera.banking.card.util.CardSecurityUtils.hashSecret("555"));
        cardRepository.save(card);
        issuedCardCvv = "555";
        rawCardNumber = card.getCardNumber();
    }

    @When("the customer activates the card with valid CVV and new PIN {string}")
    public void theCustomerActivatesTheCardWithValidCvvAndNewPin(String pin) {
        CardActivationRequest req = new CardActivationRequest(currentCard.getCardId(), issuedCardCvv, pin);
        currentCard = cardService.activateDebitCard(req);
    }

    @And("the failed PIN attempts counter should be 0")
    public void theFailedPinAttemptsCounterShouldBe0() {
        CardResponse fetched = cardService.getCardById(currentCard.getCardId());
        assertThat(fetched.getFailedPinAttempts()).isEqualTo(0);
    }
}
