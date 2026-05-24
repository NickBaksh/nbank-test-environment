package iteration_one.api_tests.deposit_test_cases;

import generators.RandomModelGenerator;
import generators.TestUser;
import iteration_one.api_tests.BaseTest;
import models.Account;
import models.DepositRequest;
import models.DepositResponse;
import models.comparison.ModelAssertions;
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

import static org.hamcrest.Matchers.equalTo;
import static specs.ResponseSpecs.*;

public class DepositOperationsTest extends BaseTest {

    static Stream<InvalidDepositCase> invalidDepositCases() {
        return Stream.of(
                new InvalidDepositCase(-0.01, DEPOSIT_AMOUNT_MIN_ERROR),
                new InvalidDepositCase(0, DEPOSIT_AMOUNT_MIN_ERROR),
                new InvalidDepositCase(5000.01, DEPOSIT_AMOUNT_MAX_ERROR)
        );
    }

    static Stream<Double> validDepositAmounts() {
        return Stream.of(0.01, 4999.99, 5000.0);
    }


    @ParameterizedTest
    @MethodSource("invalidDepositCases")
    @DisplayName("Проверка невозможности разместить невалидную сумму на счёте. 0 < депозит <= 5000")
    public void userCanNotDepositInvalidSumTest(InvalidDepositCase testCase) {

        // Использую паттерн Arrange-Act-Assert(AAA) для проверки результатов теста
        // проверяю состояние до запуска теста
        double balanceExpected = getKateFirstAccountBalance();
        int transactionsCountExpected = getKateFirstAccountTransactionsCount();

        // Пробую положить на аккаунт невалидную сумму
        DepositRequest request = DepositRequest.builder()
                .id(firstAccountId(TestUser.KATE.getKey()))
                .balance(testCase.amount)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request)
                .body(equalTo(testCase.expectedError));

        // проверяю состояние после запуска теста
        double balanceActual = getKateFirstAccountBalance();
        int transactionsCountActual = getKateFirstAccountTransactionsCount();

        softly.assertThat(balanceActual)
                .as("Account balance should not change")
                .isEqualTo(balanceExpected);

        softly.assertThat(transactionsCountActual)
                .as("Account transactions count should not change")
                .isEqualTo(transactionsCountExpected);
    }

    @ParameterizedTest
    @MethodSource("validDepositAmounts")
    @DisplayName("Проверка размещения на депозите валидных сумм. 0 < депозит <= 5000")
    public void userCanDepositValidSumTest(double deposit) {
        Account accountBefore = getFirstKateAccount();
        long accountId = accountBefore.getId();

        double balanceExpected = accountBefore.getBalance() + deposit;
        int transactionsCountExpected = accountBefore.getTransactions().size() + 1;

        DepositRequest request = DepositRequest.builder()
                .id(accountId)
                .balance(deposit)
                .build();

        DepositResponse response = new ValidatedCrudRequester<DepositResponse>(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request);

        // Сравниваю через ModelAssertion модели запроса и ответа
        ModelAssertions.assertThatModels(request, response).match();

        Double balanceActual = response.getBalance();
        int transactionCountActual = response.getTransactions().size();

        softly.assertThat(balanceActual)
                .as("Balance should increase by " + deposit)
                .isEqualTo(balanceExpected);

        softly.assertThat(transactionCountActual)
                .as("Transaction count should increase by 1")
                .isEqualTo(transactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка невозможности разместить депозит на чужом аккаунте")
    public void userCannotDepositToAnotherUsersAccountsTest() {
        double kateBalanceExpected = getKateFirstAccountBalance();
        int kateTransactionsCountExpected = getKateFirstAccountTransactionsCount();

        double alexBalanceExpected = getAlexFirstAccountBalance();
        int alexTransactionsCountExpected = getAlexFirstAccountTransactionsCount();

        DepositRequest request = RandomModelGenerator.generateWithBuilder(DepositRequest.class);
        request.setId(firstAccountId(TestUser.ALEX.getKey()));

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.returnsForbidden(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request)
                .body(equalTo(UNAUTHORIZED_ACCESS_ERROR));

        double kateBalanceActual = getKateFirstAccountBalance();
        int kateTransactionsCountActual = getKateFirstAccountTransactionsCount();

        double alexBalanceActual = getAlexFirstAccountBalance();
        int alexTransactionsCountActual = getAlexFirstAccountTransactionsCount();

        // Проверяю, что у Кейт и Алекса не был размещен депозит
        softly.assertThat(kateBalanceActual)
                .as("Account balance should not change")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Account transactions count should not change")
                .isEqualTo(kateTransactionsCountExpected);

        softly.assertThat(alexBalanceActual)
                .as("Account balance should not change")
                .isEqualTo(alexBalanceExpected);

        softly.assertThat(alexTransactionsCountActual)
                .as("Account transactions count should not change")
                .isEqualTo(alexTransactionsCountExpected);
    }

    @Test
    @DisplayName("Проверка невозможности разместить депозит на несуществующем аккаунте")
    public void cannotDepositToNonExistentAccountTest() {
        double kateBalanceExpected = getKateFirstAccountBalance();
        int kateTransactionsCountExpected = getKateFirstAccountTransactionsCount();

        DepositRequest request = RandomModelGenerator.generateWithBuilder(DepositRequest.class);
        request.setId(NON_EXISTENT_ACCOUNT_ID);

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(getKateToken()),
                ResponseSpecs.returnsForbidden(),
                Endpoint.ACCOUNTS_DEPOSIT)
                .create(request)
                .body(equalTo(UNAUTHORIZED_ACCESS_ERROR));

        double kateBalanceActual = getKateFirstAccountBalance();
        int kateTransactionsCountActual = getKateFirstAccountTransactionsCount();

        softly.assertThat(kateBalanceActual)
                .as("Account balance should not change")
                .isEqualTo(kateBalanceExpected);

        softly.assertThat(kateTransactionsCountActual)
                .as("Account transactions count should not change")
                .isEqualTo(kateTransactionsCountExpected);
    }

    // Подготавливаю тестовые данные
    public record InvalidDepositCase(double amount, String expectedError) {
    }
}
