package iteration_one.deposit_test_cases;

import iteration_one.BaseTest;
import models.Account;
import models.DepositRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import requests.requesters.post.DepositRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.hamcrest.Matchers.equalTo;
import static specs.ResponseSpecs.*;

public class DepositOperationsTest extends BaseTest {


    //Был вариант вынести текст сообщения в @CsvSource, но тогда не получится использовать константу.
    //Поэтому вернул разделение на 2 теста
    @ParameterizedTest
    @CsvSource(
            {
                    "-0.01",
                    "0"
            }
    )
    @DisplayName("Проверка невозможности разместить невалидную сумму на счёте. 0 < депозит <= 5000")
    public void userCanNotDepositInvalidSumTest(double deposit) {

        // Использую паттерн Arrange-Act-Assert(AAA) для проверки результатов теста
        // проверяю состояние до запуска теста
        double balanceExpected = getKateFirstAccountBalance();
        int transactionsCountExpected = getKateFirstAccountTransactionsCount();

        // Пробую положить на аккаунт невалидную сумму
        DepositRequest request = DepositRequest.builder()
                .id(firstAccountId(USER_KATE))
                .balance(deposit)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .post(request)
                .body(equalTo(DEPOSIT_AMOUNT_MIN_ERROR));

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

    // Оставил отдельный тест для кейса с превышением максимальной суммы, заменил хардкод на константу
    @Test
    @DisplayName("Проверка невозможности разместить сумму больше 5000 на счёте. 0 < депозит <= 5000")
    public void userCanNotDepositSumAbove5000Test() {
        double balanceExpected = getKateFirstAccountBalance();
        int transactionsCountExpected = getKateFirstAccountTransactionsCount();

        DepositRequest request = DepositRequest.builder()
                .id(firstAccountId(USER_KATE))
                .balance(5000.01)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .post(request)
                .body(equalTo(DEPOSIT_AMOUNT_MAX_ERROR));

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
    @CsvSource(
            {
                    "0.01",
                    "4999.99",
                    "5000"
            }
    )
    @DisplayName("Проверка размещения на депозите валидных сумм. 0 < депозит <= 5000")
    public void userCanDepositValidSumTest(double deposit) {
        Account accountBefore = getFirstKateAccount();
        int accountId = accountBefore.getId();

        double balanceExpected = accountBefore.getBalance() + deposit;
        int transactionsCountExpected = accountBefore.getTransactions().size() + 1;

        DepositRequest request = DepositRequest.builder()
                .id(accountId)
                .balance(deposit)
                .build();

        Account response = new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.requestReturnsOK())
                .post(request)
                .extract()
                .as(Account.class);

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

        DepositRequest request = DepositRequest.builder()
                .id(firstAccountId(USER_ALEX))
                .balance(1)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsForbidden())
                .post(request)
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

        DepositRequest request = DepositRequest.builder()
                .id(NON_EXISTENT_ACCOUNT_ID)
                .balance(1)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsForbidden())
                .post(request)
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
}
