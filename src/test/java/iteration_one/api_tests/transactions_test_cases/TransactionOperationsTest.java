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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static api.generators.testdata.InvalidTransferCase.INSUFFICIENT_FUNDS;
import static api.requests.steps.UserSteps.setUpBalance;
import static api.specs.ResponseSpecs.INSUFFICIENT_FUNDS_ERROR;
import static org.hamcrest.Matchers.equalTo;


public class TransactionOperationsTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("validTransferAmounts")
    @DisplayName("Проверка отправки валидной суммы между аккаунтами")
    public void userCanTransferMoneyToAnotherAccountTest(double transferSum) {
        TestUserContext user = getCurrentUser();

        setUpBalance(user);
        String token = user.getToken();
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user) - transferSum;
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user) + 1;

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user) + transferSum;
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user) + 1;

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(transferSum)
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
                .as("Balance should decrease by " + transferSum)
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should increase by " + transferSum)
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(secondAccountTransactionsCountExpected);
    }

    @ParameterizedTest
    @MethodSource("invalidTransferAmountsApi")
    @DisplayName("Проверка невозможности отправки невалидной суммы на аккаунт другого человека")
    public void userCannotTransferInvalidSumToAnotherAccountTest(double amount, String expectedError) {
        TestUserContext user = getCurrentUser();

        setUpBalance(user);
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
    public void userCannotTransferMoreMoneyThanHaveTest() {
        TestUserContext user = getCurrentUser();

        setUpBalance(user);
        String token = user.getToken();
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
    public void userCannotTransferMoneyToNonExistentAccountTest() {
        TestUserContext user = getCurrentUser();

        setUpBalance(user);
        String token = user.getToken();
        long firstAccountId = user.getFirstAccountId();

        double validAmount = getValidTransferAmount();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(NON_EXISTENT_ACCOUNT_ID)
                .amount(validAmount)
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
    public void userCannotTransferMoneyWithoutAuthorizationTest() {
        TestUserContext user = getCurrentUser();

        setUpBalance(user);
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
    @DisplayName("Проверка перевода от одного клиента другому. Возврат от Алекса к Кейт")
    public void userCanTransferMoneyToSenderTest() {
        TestUserContext user = getCurrentUser();

        setUpBalance(user);
        String token = user.getToken();
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user) + TRANSACTION_1;
        int firstAccountTransactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user) + 1;

        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user) - TRANSACTION_1;
        int secondAccountTransactionsCountExpected = UserSteps.getSecondAccountTransactionsCount(user) + 1;

        TransferRequest request = TransferRequest.builder()
                .senderAccountId(secondAccountId)
                .receiverAccountId(firstAccountId)
                .amount(TRANSACTION_1)
                .build();

        TransferResponse response = new ValidatedCrudRequester<TransferResponse>(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_TRANSFER)
                .create(request);

        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        int firstAccountTransactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);
        int secondAccountTransactionsCountActual = UserSteps.getSecondAccountTransactionsCount(user);


        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(firstAccountBalanceActual)
                .as("Balance should increase by 1")
                .isEqualTo(firstAccountBalanceExpected);

        softly.assertThat(firstAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(firstAccountTransactionsCountExpected);

        softly.assertThat(secondAccountBalanceActual)
                .as("Balance should decrease by 1")
                .isEqualTo(secondAccountBalanceExpected);

        softly.assertThat(secondAccountTransactionsCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(secondAccountTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка перевода со своего аккаунта на тот же аккаунт " +
            "(баланс не меняется, но транзакции создаются)")
    public void userCannotTransferMoneyToSameAccountTest() {
        TestUserContext user = getCurrentUser();

        setUpBalance(user);
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