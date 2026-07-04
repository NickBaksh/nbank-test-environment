package iteration_one.api_tests.deposit_test_cases;

import api.BaseTest;
import api.generators.RandomModelGenerator;
import api.models.dto_model.DepositRequest;
import api.models.dto_model.DepositResponse;
import api.models.comparison.ModelAssertions;
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

import static api.specs.ResponseSpecs.UNAUTHORIZED_ACCESS_ERROR;
import static org.hamcrest.Matchers.equalTo;

public class DepositOperationsTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("invalidDepositAmountsV2Api")
    @DisplayName("Проверка невозможности разместить невалидную сумму на счёте. 0 < депозит <= 5000")
    @UserSession
    @Browsers({"chrome"})
    @Environments
    @ApiVersion("v2")
    public void userCanNotDepositInvalidSumTest(double amount, String expectedError) {

        TestUserContext user = getCurrentUser();
        String token = user.getToken();

        // Использую паттерн Arrange-Act-Assert(AAA) для проверки результатов теста
        // проверяю состояние до запуска теста
        double balanceExpected = UserSteps.getFirstAccountBalance(user);
        int transactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        // Пробую положить на аккаунт невалидную сумму
        DepositRequest request = DepositRequest.builder()
                .id(user.getFirstAccountId())
                .balance(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request)
                .body(equalTo(expectedError));

        // проверяю состояние после запуска теста
        double balanceActual = UserSteps.getFirstAccountBalance(user);
        int transactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        softly.assertThat(balanceActual)
                .as("Account balance should not change")
                .isEqualTo(balanceExpected);

        softly.assertThat(transactionsCountActual)
                .as("Account transactions count should not change")
                .isEqualTo(transactionsCountExpected);

        DataBaseSteps.verifyAccountUnchanged(softly, user.getFirstAccountId(), balanceExpected, transactionsCountExpected);
    }


    @ParameterizedTest
    @MethodSource("invalidDepositAmountsV1Api")
    @DisplayName("Проверка невозможности разместить невалидную сумму на счёте. 0 < депозит <= 5000")
    @UserSession
    @Browsers({"chrome"})
    @Environments
    public void userCanNotDepositInvalidSumV1Test(double amount, String expectedError) {

        TestUserContext user = getCurrentUser();
        String token = user.getToken();

        // Использую паттерн Arrange-Act-Assert(AAA) для проверки результатов теста
        // проверяю состояние до запуска теста
        double balanceExpected = UserSteps.getFirstAccountBalance(user);
        int transactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        // Пробую положить на аккаунт невалидную сумму
        DepositRequest request = DepositRequest.builder()
                .id(user.getFirstAccountId())
                .balance(amount)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request)
                .body(equalTo(expectedError));

        // проверяю состояние после запуска теста
        double balanceActual = UserSteps.getFirstAccountBalance(user);
        int transactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        softly.assertThat(balanceActual)
                .as("Account balance should not change")
                .isEqualTo(balanceExpected);

        softly.assertThat(transactionsCountActual)
                .as("Account transactions count should not change")
                .isEqualTo(transactionsCountExpected);

        DataBaseSteps.verifyAccountUnchanged(softly, user.getFirstAccountId(), balanceExpected, transactionsCountExpected);
    }


    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    @DisplayName("Проверка размещения на депозите валидных сумм. 0 < депозит <= 5000")
    @UserSession
    public void userCanDepositValidSumTest(double deposit) {
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        long accountId = user.getFirstAccountId();
        double balanceExpected = UserSteps.getFirstAccountBalance(user) + deposit;
        int transactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user) + 1;

        DepositRequest request = DepositRequest.builder()
                .id(accountId)
                .balance(deposit)
                .build();

        DepositResponse response = new ValidatedCrudRequester<DepositResponse>(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request);

        // Сравниваю через ModelAssertion модели запроса и ответа
        ModelAssertions.assertThatModels(request, response).match();

        Double balanceActual = UserSteps.getFirstAccountBalance(user);
        int transactionCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        softly.assertThat(balanceActual)
                .as("Balance should increase by " + deposit)
                .isEqualTo(balanceExpected);

        softly.assertThat(transactionCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(transactionsCountExpected);

        DataBaseSteps.verifyDepositSaved(softly, accountId, balanceExpected, deposit);
    }

    @Test
    @DisplayName("Проверка невозможности разместить депозит на несуществующем аккаунте")
    @UserSession
    public void cannotDepositToNonExistentAccountTest() {
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        double balanceExpected = UserSteps.getFirstAccountBalance(user);
        int transactionsCountExpected = UserSteps.getFirstAccountTransactionsCount(user);

        DepositRequest request = RandomModelGenerator.generateWithBuilder(DepositRequest.class);
        request.setId(NON_EXISTENT_ACCOUNT_ID);

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsForbidden(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request)
                .body(equalTo(UNAUTHORIZED_ACCESS_ERROR));

        double balanceActual = UserSteps.getFirstAccountBalance(user);
        int transactionsCountActual = UserSteps.getFirstAccountTransactionsCount(user);

        softly.assertThat(balanceActual)
                .as("Account balance should not change")
                .isEqualTo(balanceExpected);

        softly.assertThat(transactionsCountActual)
                .as("Account transactions count should not change")
                .isEqualTo(transactionsCountExpected);
    }
}
