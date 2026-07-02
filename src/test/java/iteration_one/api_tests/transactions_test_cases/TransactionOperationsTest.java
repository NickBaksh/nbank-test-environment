package iteration_one.api_tests.transactions_test_cases;

import api.BaseTest;
import api.models.TransferRequest;
import api.models.TransferResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.annotations.ApiVersion;
import common.annotations.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static api.generators.testdata.InvalidTransferCase.INSUFFICIENT_FUNDS;
import static api.specs.ResponseSpecs.INSUFFICIENT_FUNDS_ERROR;
import static org.hamcrest.Matchers.equalTo;


public class TransactionOperationsTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    @DisplayName("Проверка отправки валидной суммы на другой аккаунт")
    @UserSession
    public void userCanTransferMoneyToAnotherAccountTest(double amount) {
        TestUserContext user = getCurrentUser();
        UserSteps.setUpBalance(user);

        String token = user.getToken();
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user) - amount;
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user) + 1;

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user) + amount;
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user) + 1;

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(amount)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        ModelAssertions.assertThatModels(request, response).match();

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountActual = UserSteps.getSecondAccountTransactionsCount(user);

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should decrease by " + amount)
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should increase by " + amount)
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(secondAccountTransactionsCountExpected);
    }

    @ParameterizedTest
    @MethodSource("invalidTransferAmountsApiV2")
    @DisplayName("Проверка невозможности отправки невалидной суммы. Тест для API v2")
    @UserSession
    @ApiVersion("v2")
    public void userCannotTransferInvalidSumToAnotherAccountApiV2Test(double amount, String expectedError) {
        TestUserContext user = getCurrentUser();
        UserSteps.setUpBalance(user);

        String token = user.getToken();
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user);


        TransferRequest request = TransferRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request)
                .body(equalTo(expectedError));

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountActual = UserSteps.getSecondAccountTransactionsCount(user);

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(secondAccountTransactionsCountExpected);
    }


    @ParameterizedTest
    @MethodSource("invalidTransferAmountsApiV1")
    @DisplayName("Проверка невозможности отправки невалидной суммы. Тест для API v1")
    @UserSession
    public void userCannotTransferInvalidSumToAnotherAccountApiV1Test(double amount, String expectedError) {
        TestUserContext user = getCurrentUser();
        UserSteps.setUpBalance(user);

        String token = user.getToken();
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user);


        TransferRequest request = TransferRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request)
                .body(equalTo(expectedError));

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountActual = UserSteps.getSecondAccountTransactionsCount(user);

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(secondAccountTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка невозможности отправки суммы больше чем есть на счёте")
    @UserSession
    public void userCannotTransferMoreMoneyThanHaveTest() {
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        UserSteps.setUpBalance(user);
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user);

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(secondAccountId)
                .receiverAccountId(firstAccountId)
                .amount(TRANSACTION_10000)
                .build();


        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request)
                .body(equalTo(INSUFFICIENT_FUNDS_ERROR));

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountActual = UserSteps.getSecondAccountTransactionsCount(user);

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(secondAccountTransactionsCountExpected);
    }


    @Test
    @DisplayName("Проверка невозможности перевода на несуществующий счёт")
    @UserSession
    public void userCannotTransferMoneyToNonExistentAccountTest() {
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        UserSteps.setUpBalance(user);
        long firstAccountId = user.getFirstAccountId();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(NON_EXISTENT_ACCOUNT_ID)
                .amount(getValidTransferAmount())
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request)
                .body(equalTo(INSUFFICIENT_FUNDS_ERROR));


        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);


        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка невозможности перевода без авторизации")
    @UserSession
    public void userCannotTransferMoneyWithoutAuthorizationTest() {
        TestUserContext user = getCurrentUser();
        UserSteps.setUpBalance(user);

        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user);

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(INSUFFICIENT_FUNDS.generate())
                .build();

        new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.returnsUnauthorize(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountActual = UserSteps.getSecondAccountTransactionsCount(user);

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should not decrease")
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should not increase")
                .isEqualTo(secondAccountTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка перевода со своего аккаунта на тот же аккаунт " +
            "(баланс не меняется, но транзакции создаются)")
    @UserSession
    public void userCannotTransferMoneyToSameAccountTest() {
        TestUserContext user = getCurrentUser();
        UserSteps.setUpBalance(user);

        String token = user.getToken();
        long firstAccountId = user.getFirstAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user) + 2;

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(firstAccountId)
                .amount(TRANSACTION_100)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should not change")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should increase by 2")
                .isEqualTo(firstAccountTransactionsCountExpected);
    }
}