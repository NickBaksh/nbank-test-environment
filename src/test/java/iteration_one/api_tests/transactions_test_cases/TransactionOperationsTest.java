package iteration_one.api_tests.transactions_test_cases;

import generators.TestUser;
import iteration_one.api_tests.BaseTest;
import models.Account;
import models.DepositRequest;
import models.TransferRequest;
import models.TransferResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

import static generators.testdata.InvalidTransferCase.INSUFFICIENT_FUNDS;
import static org.hamcrest.Matchers.equalTo;
import static specs.RequestSpecs.*;
import static specs.ResponseSpecs.*;


public class TransactionOperationsTest extends BaseTest {
    // Тестовые данные для проверки транзакций невалидных сумм
    static Stream<TransactionOperationsTest.InvalidTransferCase> invalidTransferCases() {
        return Stream.of(
                new TransactionOperationsTest.InvalidTransferCase(-0.01, TRANSFER_AMOUNT_MIN_ERROR),
                new TransactionOperationsTest.InvalidTransferCase(0, TRANSFER_AMOUNT_MIN_ERROR),
                new TransactionOperationsTest.InvalidTransferCase(10000.01, TRANSFER_AMOUNT_MAX_ERROR)
        );
    }

    // Тестовые данные для проверки транзакций валидных сумм
    static Stream<Double> validTransferAmounts() {
        return Stream.of(0.01, 9999.99, 10000.0);
    }

    @BeforeEach
    public void setUpBalance() {
        Account accountKate = getFirstKateAccount();
        long kateAccountId = accountKate.getId();

        //Пополняем счёт Кейт на 20000 перед каждым тестом перевода
        //Вызов несколько раз, т.к. есть ограничение на пополнение в 5000
        repeat(4, () -> {
            DepositRequest request = DepositRequest.builder()
                    .id(kateAccountId)
                    .balance(BALANCE_5000)
                    .build();

            new CrudRequester(
                    RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.ACCOUNTS_DEPOSIT)
                    .create(request);
        });

        Account accountAlex = getFirstAlexAccount();
        long alexAccountId = accountAlex.getId();

        DepositRequest request = DepositRequest.builder()
                .id(alexAccountId)
                .balance(BALANCE_5000)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token(TestUser.ALEX.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request);
    }

    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    @DisplayName("Проверка отправки валидной суммы на аккаунт другого человека")
    public void userCanTransferMoneyToAnotherAccountTest(double transferSum) {
        Account accountKateBefore = getFirstKateAccount();
        long kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        long alexAccountId = accountAlexBefore.getId();

        double kateBalanceExpected = accountKateBefore.getBalance() - transferSum;
        int kateTransactionsCountExpected = accountKateBefore.getTransactions().size() + 1;

        double alexBalanceExpected = accountAlexBefore.getBalance() + transferSum;
        int alexTransactionsCountExpected = accountAlexBefore.getTransactions().size() + 1;

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(transferSum)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceActual = accountKateAfter.getBalance();
        int kateTransactionsCountActual = accountKateAfter.getTransactions().size();

        double alexBalanceActual = accountAlexAfter.getBalance();
        int alexTransactionsCountActual = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceActual)
                .as("Balance should decrease by " + transferSum)
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(kateTransactionsCountExpected);

        softly.assertThat(alexBalanceActual)
                .as("Balance should increase by " + transferSum)
                .isEqualTo(alexBalanceExpected);

        softly.assertThat(alexTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(alexTransactionsCountExpected);
    }

    @ParameterizedTest
    @MethodSource("invalidTransferCases")
    @DisplayName("Проверка невозможности отправки невалидной суммы на аккаунт другого человека")
    public void userCannotTransferInvalidSumToAnotherAccountTest(InvalidTransferCase testCase) {
        Account accountKateBefore = getFirstKateAccount();
        long kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        long alexAccountId = accountAlexBefore.getId();

        double kateBalanceExpected = accountKateBefore.getBalance();
        int kateTransactionsCountExpected = accountKateBefore.getTransactions().size();

        double alexBalanceExpected = accountAlexBefore.getBalance();
        int alexTransactionsCountExpected = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(testCase.amount)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request)
                .body(equalTo(testCase.expectedError));

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceActual = accountKateAfter.getBalance();
        int kateTransactionsCountActual = accountKateAfter.getTransactions().size();

        double alexBalanceActual = accountAlexAfter.getBalance();
        int alexTransactionsCountActual = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountExpected);

        softly.assertThat(alexBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(alexBalanceExpected);

        softly.assertThat(alexTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(alexTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка невозможности отправки суммы больше чем есть на счёте")
    public void userCannotTransferMoreMoneyThanHaveTest() {
        Account accountKateBefore = getSecondKateAccount();
        long kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        long alexAccountId = accountAlexBefore.getId();

        double kateBalanceExpected = accountKateBefore.getBalance();
        int kateTransactionsCountExpected = accountKateBefore.getTransactions().size();

        double alexBalanceExpected = accountAlexBefore.getBalance();
        int alexTransactionsCountExpected = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(TRANSACTION_10000)
                .build();


        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request)
                .body(equalTo(INSUFFICIENT_FUNDS_ERROR));

        Account accountKateAfter = getSecondKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceActual = accountKateAfter.getBalance();
        int kateTransactionsCountActual = accountKateAfter.getTransactions().size();

        double alexBalanceActual = accountAlexAfter.getBalance();
        int alexTransactionsCountActual = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountExpected);

        softly.assertThat(alexBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(alexBalanceExpected);

        softly.assertThat(alexTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(alexTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка перевода между своими счетами")
    public void userCanTransferMoneyBetweenAccountsTest() {
        Account firstKateAccountBefore = getFirstKateAccount();
        long kateFirstAccountId = firstKateAccountBefore.getId();

        Account secondKateAccountBefore = getSecondKateAccount();
        long kateSecondAccountId = secondKateAccountBefore.getId();

        double kateFirstAccountBalanceExpected = firstKateAccountBefore.getBalance() - TRANSACTION_100;
        int kateFirstAccountTransactionsCountExpected = firstKateAccountBefore.getTransactions().size() + 1;

        double kateSecondAccountBalanceExpected = secondKateAccountBefore.getBalance() + TRANSACTION_100;
        int kateSecondAccountTransactionsCountExpected = secondKateAccountBefore.getTransactions().size() + 1;

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateFirstAccountId)
                .receiverAccountId(kateSecondAccountId)
                .amount(TRANSACTION_100)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        Account firstKateAccountAfter = getFirstKateAccount();
        Account secondKateAccountAfter = getSecondKateAccount();

        double kateFirstAccountBalanceActual = firstKateAccountAfter.getBalance();
        int kateFirstAccountTransactionsCountActual = firstKateAccountAfter.getTransactions().size();

        double kateSecondAccountBalanceActual = secondKateAccountAfter.getBalance();
        int kateSecondAccountTransactionsCountActual = secondKateAccountAfter.getTransactions().size();

        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(kateFirstAccountBalanceActual)
                .as("Balance should decrease by " + TRANSACTION_100)
                .isEqualTo(kateFirstAccountBalanceExpected);

        softly.assertThat(kateFirstAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(kateFirstAccountTransactionsCountExpected);

        softly.assertThat(kateSecondAccountBalanceActual)
                .as("Balance should increase by " + TRANSACTION_100)
                .isEqualTo(kateSecondAccountBalanceExpected);

        softly.assertThat(kateSecondAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(kateSecondAccountTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка невозможности перевода на несуществующий счёт")
    public void userCannotTransferMoneyToNonExistentAccountTest() {
        Account accountKateBefore = getFirstKateAccount();
        long kateAccountId = accountKateBefore.getId();

        double kateBalanceExpected = accountKateBefore.getBalance();
        int kateTransactionsCountExpected = accountKateBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(NON_EXISTENT_ACCOUNT_ID)
                .amount(INSUFFICIENT_FUNDS.generate())
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request)
                .body(equalTo(INSUFFICIENT_FUNDS_ERROR));


        Account accountKateAfter = getFirstKateAccount();

        double kateBalanceActual = accountKateAfter.getBalance();
        int kateTransactionsCountActual = accountKateAfter.getTransactions().size();

        softly.assertThat(kateBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка невозможности перевода без авторизации")
    public void userCannotTransferMoneyWithoutAuthorizationTest() {
        Account accountKateBefore = getSecondKateAccount();
        long kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        long alexAccountId = accountAlexBefore.getId();

        double kateBalanceExpected = accountKateBefore.getBalance();
        int kateTransactionsCountExpected = accountKateBefore.getTransactions().size();

        double alexBalanceExpected = accountAlexBefore.getBalance();
        int alexTransactionsCountExpected = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(alexAccountId)
                .amount(INSUFFICIENT_FUNDS.generate())
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.returnsUnauthorize(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        Account accountKateAfter = getSecondKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceActual = accountKateAfter.getBalance();
        int kateTransactionsCountActual = accountKateAfter.getTransactions().size();

        double alexBalanceActual = accountAlexAfter.getBalance();
        int alexTransactionsCountActual = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(kateTransactionsCountExpected);

        softly.assertThat(alexBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(alexBalanceExpected);

        softly.assertThat(alexTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(alexTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка перевода от одного клиента другому. Возврат от Алекса к Кейт")
    public void userCanTransferMoneyToSenderTest() {
        Account accountKateBefore = getFirstKateAccount();
        long kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        long alexAccountId = accountAlexBefore.getId();

        double kateBalanceExpected = accountKateBefore.getBalance() + TRANSACTION_1;
        int kateTransactionsCountExpected = accountKateBefore.getTransactions().size() + 1;

        double alexBalanceExpected = accountAlexBefore.getBalance() - TRANSACTION_1;
        int alexTransactionsCountExpected = accountAlexBefore.getTransactions().size() + 1;

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(alexAccountId)
                .receiverAccountId(kateAccountId)
                .amount(TRANSACTION_1)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token(TestUser.ALEX.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceActual = accountKateAfter.getBalance();
        int kateTransactionsCountActual = accountKateAfter.getTransactions().size();

        double alexBalanceActual = accountAlexAfter.getBalance();
        int alexTransactionsCountActual = accountAlexAfter.getTransactions().size();

        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(kateBalanceActual)
                .as("Balance should increase by 1")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(kateTransactionsCountExpected);

        softly.assertThat(alexBalanceActual)
                .as("Balance should decrease by 1")
                .isEqualTo(alexBalanceExpected);

        softly.assertThat(alexTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(alexTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка невозможности перевода с авторизацией на своем аккаунте, но трансфере с чужого аккаунта")
    public void userCannotTransferMoneyFromAnotherUsersAccountTest() {
        Account accountKateBefore = getFirstKateAccount();
        long kateAccountId = accountKateBefore.getId();

        Account accountAlexBefore = getFirstAlexAccount();
        long alexAccountId = accountAlexBefore.getId();

        double kateBalanceExpected = accountKateBefore.getBalance();
        int kateTransactionsCountExpected = accountKateBefore.getTransactions().size();

        double alexBalanceExpected = accountAlexBefore.getBalance();
        int alexTransactionsCountExpected = accountAlexBefore.getTransactions().size();

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(alexAccountId)
                .receiverAccountId(kateAccountId)
                .amount(INSUFFICIENT_FUNDS.generate())
                .build();


        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.returnsForbidden(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request)
                .body(equalTo(UNAUTHORIZED_ACCESS_ERROR));

        Account accountKateAfter = getFirstKateAccount();
        Account accountAlexAfter = getFirstAlexAccount();

        double kateBalanceActual = accountKateAfter.getBalance();
        int kateTransactionsCountActual = accountKateAfter.getTransactions().size();

        double alexBalanceActual = accountAlexAfter.getBalance();
        int alexTransactionsCountActual = accountAlexAfter.getTransactions().size();

        softly.assertThat(kateBalanceActual)
                .as("Kate balance should not decrease")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Kate transaction count should not increase")
                .isEqualTo(kateTransactionsCountExpected);

        softly.assertThat(alexBalanceActual)
                .as("Alex balance should not decrease")
                .isEqualTo(alexBalanceExpected);

        softly.assertThat(alexTransactionsCountActual)
                .as("Alex transaction count should not increase")
                .isEqualTo(alexTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка перевода со своего аккаунта на тот же аккаунт " +
            "(баланс не меняется, но транзакции создаются)")
    public void userCannotTransferMoneyToSameAccountTest() {
        Account accountKateBefore = getFirstKateAccount();
        long kateAccountId = accountKateBefore.getId();

        double kateBalanceExpected = accountKateBefore.getBalance();
        long kateTransactionsCountExpected = accountKateBefore.getTransactions().size() + 2;

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(kateAccountId)
                .receiverAccountId(kateAccountId)
                .amount(TRANSACTION_100)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        Account accountKateAfter = getFirstKateAccount();

        double kateBalanceActual = accountKateAfter.getBalance();
        int kateTransactionsCountActual = accountKateAfter.getTransactions().size();

        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(kateBalanceActual)
                .as("Balance should not change")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Transaction count should increase by 2")
                .isEqualTo(kateTransactionsCountExpected);
    }

    // Подготавливаю тестовые данные
    public record InvalidTransferCase(double amount, String expectedError) {
    }
}