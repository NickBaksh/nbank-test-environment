package iteration_one.api_tests.deposit_test_cases;

import api.BaseTest;
import api.configs.Config;
import api.generators.RandomModelGenerator;
import api.models.comparison.ModelAssertions;
import api.models.dto_model.DepositRequest;
import api.models.dto_model.DepositResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.annotations.ApiVersion;
import common.annotations.Browsers;
import common.annotations.Environments;
import common.annotations.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static api.constants.JsonFields.MESSAGE;
import static api.generators.testdata.DataProviders.NON_EXISTENT_ACCOUNT_ID;
import static api.specs.ResponseSpecs.UNAUTHORIZED_ACCESS_ERROR;
import static org.hamcrest.Matchers.equalTo;

public class DepositOperationsTest extends BaseTest {

    // ==================== Невалидные суммы ====================

    @ParameterizedTest
    @MethodSource("invalidDepositAmountsV2Api")
    @DisplayName("Невозможно пополнить счёт невалидной суммой (депозит <= 0 или > 5000), API v2")
    @UserSession
    @Browsers({"chrome"})
    @Environments
    @ApiVersion("v2")
    public void userCanNotDepositInvalidSumTest(double amount, String expectedError) {
        verifyInvalidDepositRejected(amount, expectedError);
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAmountsV1Api")
    @DisplayName("Невозможно пополнить счёт невалидной суммой (депозит <= 0 или > 5000), API v1")
    @UserSession
    @Browsers({"chrome"})
    @Environments
    public void userCanNotDepositInvalidSumV1Test(double amount, String expectedError) {
        verifyInvalidDepositRejected(amount, expectedError);
    }

    // ==================== Валидные суммы ====================

    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    @DisplayName("Пополнение счёта валидной суммой (0 < депозит <= 5000)")
    @UserSession
    public void userCanDepositValidSumTest(double deposit) {
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        long accountId = user.getFirstAccountId();
        double balanceExpected = UserSteps.getFirstAccountBalance(user) + deposit;
        int transactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user) + 1;

        DepositRequest request = DepositRequest.builder()
                .accountId(accountId)
                .amount(deposit)
                .build();

        DepositResponse response = new ValidatedCrudRequester<DepositResponse>(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request);

        // Сравниваю через ModelAssertion модели запроса и ответа
        ModelAssertions.assertThatModels(request, response).match();

        softly.assertThat(response.getDepositAmount())
                .as("Deposit amount in response")
                .isEqualTo(deposit);
        softly.assertThat(response.getTransactionId())
                .as("Deposit transaction should be created")
                .isNotNull();

        double balanceActual = UserSteps.getFirstAccountBalance(user);
        int transactionCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        softly.assertThat(balanceActual)
                .as("Balance should increase by " + deposit)
                .isEqualTo(balanceExpected);

        softly.assertThat(transactionCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(transactionsCountExpected);

        if (Config.isDbChecksEnabled()) {
            DataBaseSteps.verifyDepositSaved(softly, accountId, balanceExpected, deposit);
        }
    }

    // ==================== Несуществующий счёт ====================

    @Test
    @DisplayName("Невозможно пополнить несуществующий счёт")
    @UserSession
    public void cannotDepositToNonExistentAccountTest() {
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        double balanceExpected = UserSteps.getFirstAccountBalance(user);
        int transactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        DepositRequest request = RandomModelGenerator.generateWithBuilder(DepositRequest.class);
        request.setAccountId(NON_EXISTENT_ACCOUNT_ID);

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsForbidden(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request)
                .body(MESSAGE, equalTo(UNAUTHORIZED_ACCESS_ERROR));

        verifyAccountUnchanged(user, balanceExpected, transactionsCountExpected);
    }

    // ==================== Общие шаги ====================

    /**
     * Пробует пополнить первый счёт пользователя невалидной суммой и проверяет,
     * что сервер вернул 400 с ожидаемым текстом, а счёт не изменился.
     */
    private void verifyInvalidDepositRejected(double amount, String expectedError) {
        TestUserContext user = getCurrentUser();

        // Arrange: состояние до запроса
        double balanceExpected = UserSteps.getFirstAccountBalance(user);
        int transactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        DepositRequest request = DepositRequest.builder()
                .accountId(user.getFirstAccountId())
                .amount(amount)
                .build();

        // Act + Assert: сервер отклоняет запрос
        new CrudRequester(
                RequestSpecs.authWithTokenSpec(user.getToken()),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request)
                .body(MESSAGE, equalTo(expectedError));

        // Assert: счёт не изменился
        verifyAccountUnchanged(user, balanceExpected, transactionsCountExpected);
    }

    /** Проверяет через API (и в БД, если она доступна), что баланс и число транзакций не изменились. */
    private void verifyAccountUnchanged(TestUserContext user, double balanceExpected, int transactionsCountExpected) {
        softly.assertThat(UserSteps.getFirstAccountBalance(user))
                .as("Account balance should not change")
                .isEqualTo(balanceExpected);

        softly.assertThat(UserSteps.getFirstAccountTransactionsCount(user))
                .as("Account transactions count should not change")
                .isEqualTo(transactionsCountExpected);

        if (Config.isDbChecksEnabled()) {
            DataBaseSteps.verifyAccountUnchanged(softly, user.getFirstAccountId(),
                    balanceExpected, transactionsCountExpected);
        }
    }
}