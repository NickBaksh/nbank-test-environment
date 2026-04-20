package iteration_one.transactions_test_cases;

import iteration_one.BaseTest;
import models.Account;
import models.DepositRequest;
import models.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import requests.requesters.post.DepositRequester;
import requests.requesters.post.TransferRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.hamcrest.Matchers.equalTo;


public class TransactionOperationsTest extends BaseTest {
    @BeforeEach
    public void setUpBalance() {
        Account accountKate = getFirstKateAccount();
        int kateAccountId = accountKate.getId();

        //Пополняем счёт Кейт на 20000 перед каждым тестом перевода
        //Вызов несколько раз, т.к. есть ограничение на пополнение в 5000
        for (int i = 0; i < 4; i++) {
            DepositRequest request = DepositRequest.builder()
                    .id(kateAccountId)
                    .balance(5000.00)
                    .build();

            new DepositRequester(
                    RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                    ResponseSpecs.requestReturnsOK())
                    .post(request);
        }

        Account accountAlex = getFirstAlexAccount();
        int alexAccountId = accountAlex.getId();

        DepositRequest request = DepositRequest.builder()
                .id(alexAccountId)
                .balance(5000.00)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_ALEX)),
                ResponseSpecs.requestReturnsOK())
                .post(request);
    }

    @ParameterizedTest
    @CsvSource({
            "0.01",
            "9999.99",
            "10000"
    })
    @DisplayName("Проверка отправки валидной суммы на аккаунт другого человека")
    public void userCanTransferMoneyToAnotherAccountTest(double transferSum) {
        Account accountKateBefore = getFirstKateAccount();
        int kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        int alexAccountId = accountAlexBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        double alexBalanceBefore = accountAlexBefore.getBalance();
        int alexTransactionsCountBefore = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(transferSum)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.requestReturnsOK())
                .post(request)
                .body("message", equalTo("Transfer successful"));

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        double alexBalanceAfter = accountAlexAfter.getBalance();
        int alexTransactionsCountAfter = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should decrease by " + transferSum)
                .isEqualTo(kateBalanceBefore - transferSum);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should increase by 1")
                .isEqualTo(kateTransactionsCountBefore + 1);

        softly.assertThat(alexBalanceAfter)
                .as("Balance should increase by " + transferSum)
                .isEqualTo(alexBalanceBefore + transferSum);

        softly.assertThat(alexTransactionsCountAfter)
                .as("Transaction count should increase by 1")
                .isEqualTo(alexTransactionsCountBefore + 1);
    }


    @ParameterizedTest
    @CsvSource({
            "-0.01",
            "0"
    })
    @DisplayName("Проверка невозможности отправки невалидной суммы на аккаунт другого человека")
    public void userCannotTransferInvalidSumToAnotherAccountTest(double transferSum) {
        Account accountKateBefore = getFirstKateAccount();
        int kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        int alexAccountId = accountAlexBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        double alexBalanceBefore = accountAlexBefore.getBalance();
        int alexTransactionsCountBefore = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(transferSum)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .post(request)
                .body(equalTo("Transfer amount must be at least 0.01"));

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        double alexBalanceAfter = accountAlexAfter.getBalance();
        int alexTransactionsCountAfter = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceBefore);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountBefore);

        softly.assertThat(alexBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(alexBalanceBefore);

        softly.assertThat(alexTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(alexTransactionsCountBefore);
    }


    @Test
    @DisplayName("Проверка невозможности отправки суммы больше 10000")
    public void userCannotTransferMoreMoneyThan10000Test() {
        Account accountKateBefore = getFirstKateAccount();
        int kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        int alexAccountId = accountAlexBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        double alexBalanceBefore = accountAlexBefore.getBalance();
        int alexTransactionsCountBefore = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(10000.01)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .post(request)
                .body(equalTo("Transfer amount cannot exceed 10000"));

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        double alexBalanceAfter = accountAlexAfter.getBalance();
        int alexTransactionsCountAfter = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceBefore);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountBefore);

        softly.assertThat(alexBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(alexBalanceBefore);

        softly.assertThat(alexTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(alexTransactionsCountBefore);
    }

    @Test
    @DisplayName("Проверка невозможности отправки суммы больше чем есть на счёте")
    public void userCannotTransferMoreMoneyThanHaveTest() {
        Account accountKateBefore = getSecondKateAccount();
        int kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        int alexAccountId = accountAlexBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        double alexBalanceBefore = accountAlexBefore.getBalance();
        int alexTransactionsCountBefore = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(10000.00)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .post(request)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        Account accountKateAfter = getSecondKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        double alexBalanceAfter = accountAlexAfter.getBalance();
        int alexTransactionsCountAfter = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceBefore);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountBefore);

        softly.assertThat(alexBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(alexBalanceBefore);

        softly.assertThat(alexTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(alexTransactionsCountBefore);
    }

    @Test
    @DisplayName("Проверка перевода между своими счетами")
    public void userCanTransferMoneyBetweenAccountsTest() {
        Account firstKateAccountBefore = getFirstKateAccount();
        int kateFirstAccountId = firstKateAccountBefore.getId();

        Account secondKateAccountBefore = getSecondKateAccount();
        int kateSecondAccountId = secondKateAccountBefore.getId();

        double kateFirstAccountBalanceBefore = firstKateAccountBefore.getBalance();
        int kateFirstAccountTransactionsCountBefore = firstKateAccountBefore.getTransactions().size();

        double kateSecondAccountBalanceBefore = secondKateAccountBefore.getBalance();
        int kateSecondAccountTransactionsCountBefore = secondKateAccountBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateFirstAccountId)
                .receiverAccountId(kateSecondAccountId)
                .amount(100.00)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.requestReturnsOK())
                .post(request)
                .body("message", equalTo("Transfer successful"));

        Account firstKateAccountAfter = getFirstKateAccount();
        Account secondKateAccountAfter = getSecondKateAccount();

        double kateFirstAccountBalanceAfter = firstKateAccountAfter.getBalance();
        int kateFirstAccountTransactionsCountAfter = firstKateAccountAfter.getTransactions().size();

        double kateSecondAccountBalanceAfter = secondKateAccountAfter.getBalance();
        int kateSecondAccountTransactionsCountAfter = secondKateAccountAfter.getTransactions().size();

        softly.assertThat(kateFirstAccountBalanceAfter)
                .as("Balance should decrease by " + 100.00)
                .isEqualTo(kateFirstAccountBalanceBefore - 100.00);

        softly.assertThat(kateFirstAccountTransactionsCountAfter)
                .as("Transaction count should increase by 1")
                .isEqualTo(kateFirstAccountTransactionsCountBefore + 1);

        softly.assertThat(kateSecondAccountBalanceAfter)
                .as("Balance should increase by " + 100.00)
                .isEqualTo(kateSecondAccountBalanceBefore + 100.00);

        softly.assertThat(kateSecondAccountTransactionsCountAfter)
                .as("Transaction count should increase by 1")
                .isEqualTo(kateSecondAccountTransactionsCountBefore + 1);
    }

    @Test
    @DisplayName("Проверка невозможности перевода на несуществующий счёт")
    public void userCannotTransferMoneyToNonExistentAccountTest() {
        Account accountKateBefore = getFirstKateAccount();
        int kateAccountId = accountKateBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(NON_EXISTENT_ACCOUNT_ID)
                .amount(100.00)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .post(request)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));

        Account accountKateAfter = getFirstKateAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceBefore);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountBefore);
    }

    @Test
    @DisplayName("Проверка невозможности перевода без авторизации")
    public void userCannotTransferMoneyWithoutAuthorizationTest() {
        Account accountKateBefore = getSecondKateAccount();
        int kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        int alexAccountId = accountAlexBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        double alexBalanceBefore = accountAlexBefore.getBalance();
        int alexTransactionsCountBefore = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(100.00)
                .build();

        new TransferRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.returnsUnauthorize())
                .post(request);

        Account accountKateAfter = getSecondKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        double alexBalanceAfter = accountAlexAfter.getBalance();
        int alexTransactionsCountAfter = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceBefore);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountBefore);

        softly.assertThat(alexBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(alexBalanceBefore);

        softly.assertThat(alexTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(alexTransactionsCountBefore);
    }

    @Test
    @DisplayName("Проверка перевода от одного клиента другому. Возврат от Алекса к Кейт")
    public void userCanTransferMoneyToSenderTest() {
        Account accountKateBefore = getFirstKateAccount();
        int kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        int alexAccountId = accountAlexBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        double alexBalanceBefore = accountAlexBefore.getBalance();
        int alexTransactionsCountBefore = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(alexAccountId)
                .receiverAccountId(kateAccountId)
                .amount(1.00)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_ALEX)),
                ResponseSpecs.requestReturnsOK())
                .post(request)
                .body("message", equalTo("Transfer successful"));

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        double alexBalanceAfter = accountAlexAfter.getBalance();
        int alexTransactionsCountAfter = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should increase by 1")
                .isEqualTo(kateBalanceBefore + 1.00);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should increase by 1")
                .isEqualTo(kateTransactionsCountBefore + 1);

        softly.assertThat(alexBalanceAfter)
                .as("Balance should decrease by 1")
                .isEqualTo(alexBalanceBefore - 1.00);

        softly.assertThat(alexTransactionsCountAfter)
                .as("Transaction count should increase by 1")
                .isEqualTo(alexTransactionsCountBefore + 1);
    }

    @Test
    @DisplayName("Проверка невозможности перевода с авторизацией на своем аккаунте, но трансфере с чужого аккаунта")
    public void userCannotTransferMoneyFromAnotherUsersAccountTest() {
        Account accountKateBefore = getFirstKateAccount();
        int kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        int alexAccountId = accountAlexBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        double alexBalanceBefore = accountAlexBefore.getBalance();
        int alexTransactionsCountBefore = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(alexAccountId)
                .receiverAccountId(kateAccountId)
                .amount(100.00)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsForbidden())
                .post(request)
                .body(equalTo("Unauthorized access to account"));

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        double alexBalanceAfter = accountAlexAfter.getBalance();
        int alexTransactionsCountAfter = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceBefore);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountBefore);

        softly.assertThat(alexBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(alexBalanceBefore);

        softly.assertThat(alexTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(alexTransactionsCountBefore);
    }

    @Test
    @DisplayName("Проверка невозможности перевода со своего аккаунта на тот же аккаунт")
    public void userCannotTransferMoneyToSameAccountTest() {
        Account accountKateBefore = getFirstKateAccount();
        int kateAccountId = accountKateBefore.getId();

        double kateBalanceBefore = accountKateBefore.getBalance();
        int kateTransactionsCountBefore = accountKateBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(kateAccountId)
                .amount(100.00)
                .build();

        new TransferRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.requestReturnsOK())
                .post(request)
                .body("message", equalTo("Transfer successful"));

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceAfter = accountKateAfter.getBalance();
        int kateTransactionsCountAfter = accountKateAfter.getTransactions().size();

        softly.assertThat(kateBalanceAfter)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceBefore);

        softly.assertThat(kateTransactionsCountAfter)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountBefore + 2);
    }
}