package com.nuera.banking.card.acceptance.steps;

import com.nuera.banking.card.entity.AccountEntity;
import com.nuera.banking.card.entity.CardEntity;
import com.nuera.banking.card.model.dto.*;
import com.nuera.banking.card.model.enums.*;
import com.nuera.banking.card.repository.AccountRepository;
import com.nuera.banking.card.repository.CardRepository;
import com.nuera.banking.card.service.AccountService;
import com.nuera.banking.card.service.CardService;
import com.nuera.banking.card.service.TransactionService;
import com.nuera.banking.card.util.CardSecurityUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CardTransactionSteps {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardService cardService;

    @Autowired
    private TransactionService transactionService;

    private AccountEntity activeAccount;
    private CardEntity activeCard;
    private TransactionAuthorizationResponse lastAuthResponse;

    @Given("an active debit card with balance {double} and PIN {string}")
    public void anActiveDebitCardWithBalanceAndPin(double balance, String pin) {
        String accId = "acc-" + UUID.randomUUID().toString().substring(0, 8);
        activeAccount = new AccountEntity(
                accId, "10" + UUID.randomUUID().toString().substring(0, 8),
                "cust-acceptance", "Acceptance User", AccountType.CHECKING,
                BigDecimal.valueOf(balance), "USD", AccountStatus.ACTIVE
        );
        accountRepository.save(activeAccount);

        String crdId = "crd-" + UUID.randomUUID().toString().substring(0, 8);
        String cardNumber = CardSecurityUtils.generateCardNumber(CardNetwork.VISA);
        activeCard = new CardEntity();
        activeCard.setCardId(crdId);
        activeCard.setCardNumber(cardNumber);
        activeCard.setCardHolderName("ACCEPTANCE USER");
        activeCard.setCardType(CardType.DEBIT);
        activeCard.setCardNetwork(CardNetwork.VISA);
        activeCard.setExpiryDate(CardSecurityUtils.generateExpiryDate(3));
        activeCard.setCvvHash(CardSecurityUtils.hashSecret("123"));
        activeCard.setPinHash(CardSecurityUtils.hashSecret(pin));
        activeCard.setCardStatus(CardStatus.ACTIVE);
        activeCard.setAccountId(accId);
        activeCard.setCurrency("USD");
        activeCard.setFailedPinAttempts(0);
        activeCard.setDailyAtmWithdrawalLimit(new BigDecimal("500.00"));
        activeCard.setDailyPosPurchaseLimit(new BigDecimal("2500.00"));
        activeCard.setDailyOnlinePurchaseLimit(new BigDecimal("3000.00"));
        activeCard.setAtmWithdrawalsEnabled(true);
        activeCard.setPosTransactionsEnabled(true);
        activeCard.setOnlineTransactionsEnabled(true);
        activeCard.setInternationalUsageEnabled(false);
        activeCard.setCreatedAt(Instant.now());
        activeCard.setUpdatedAt(Instant.now());
        cardRepository.save(activeCard);
    }

    @Given("an active debit card with daily ATM limit {double} and balance {double}")
    public void anActiveDebitCardWithDailyAtmLimitAndBalance(double limit, double balance) {
        anActiveDebitCardWithBalanceAndPin(balance, "1234");
        activeCard.setDailyAtmWithdrawalLimit(BigDecimal.valueOf(limit));
        cardRepository.save(activeCard);
    }

    @Given("an active debit card with balance {double}")
    public void anActiveDebitCardWithBalance(double balance) {
        anActiveDebitCardWithBalanceAndPin(balance, "1234");
    }

    @Given("an active debit card with balance {double} and international usage disabled")
    public void anActiveDebitCardWithBalanceAndInternationalDisabled(double balance) {
        anActiveDebitCardWithBalanceAndPin(balance, "1234");
        activeCard.setInternationalUsageEnabled(false);
        cardRepository.save(activeCard);
    }

    @When("an ATM withdrawal of {double} is requested with PIN {string}")
    public void anAtmWithdrawalIsRequested(double amount, String pin) {
        TransactionAuthorizationRequest req = new TransactionAuthorizationRequest(
                activeCard.getCardNumber(), pin, BigDecimal.valueOf(amount),
                TransactionType.ATM_WITHDRAWAL, TransactionChannel.ATM
        );
        lastAuthResponse = transactionService.authorizeTransaction(req);
    }

    @When("a POS purchase of {double} is requested with PIN {string}")
    public void aPosPurchaseIsRequested(double amount, String pin) {
        TransactionAuthorizationRequest req = new TransactionAuthorizationRequest(
                activeCard.getCardNumber(), pin, BigDecimal.valueOf(amount),
                TransactionType.POS_PURCHASE, TransactionChannel.POS_TERMINAL
        );
        lastAuthResponse = transactionService.authorizeTransaction(req);
    }

    @When("the cardholder updates the card status to {string}")
    public void theCardholderUpdatesTheCardStatusTo(String status) {
        CardStatusUpdateRequest req = new CardStatusUpdateRequest(CardStatus.valueOf(status), "User request");
        cardService.updateCardStatus(activeCard.getCardId(), req);
    }

    @When("a POS purchase of {double} is attempted")
    public void aPosPurchaseOfIsAttempted(double amount) {
        aPosPurchaseIsRequested(amount, "1234");
    }

    @When("an international transaction of {double} is requested")
    public void anInternationalTransactionIsRequested(double amount) {
        TransactionAuthorizationRequest req = new TransactionAuthorizationRequest(
                activeCard.getCardNumber(), "1234", BigDecimal.valueOf(amount),
                TransactionType.POS_PURCHASE, TransactionChannel.POS_TERMINAL
        );
        req.setInternational(true);
        lastAuthResponse = transactionService.authorizeTransaction(req);
    }

    @Then("the transaction should be {string}")
    public void theTransactionShouldBe(String status) {
        assertThat(lastAuthResponse.getStatus().name()).isEqualTo(status);
        if ("APPROVED".equals(status)) {
            assertThat(lastAuthResponse.isAuthorized()).isTrue();
        } else {
            assertThat(lastAuthResponse.isAuthorized()).isFalse();
        }
    }

    @Then("the transaction should be {string} with reason {string}")
    public void theTransactionShouldBeWithReason(String status, String reason) {
        assertThat(lastAuthResponse.getStatus().name()).isEqualTo(status);
        assertThat(lastAuthResponse.getDeclineReason().name()).isEqualTo(reason);
    }

    @And("an authorization code should be returned")
    public void anAuthorizationCodeShouldBeReturned() {
        assertThat(lastAuthResponse.getAuthorizationCode()).isNotBlank();
    }

    @And("the remaining account balance should be {double}")
    public void theRemainingAccountBalanceShouldBe(double expectedBalance) {
        AccountEntity updated = accountRepository.findById(activeAccount.getAccountId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(expectedBalance));
    }

    @And("the account balance should remain {double}")
    public void theAccountBalanceShouldRemain(double expectedBalance) {
        AccountEntity updated = accountRepository.findById(activeAccount.getAccountId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(expectedBalance));
    }
}
