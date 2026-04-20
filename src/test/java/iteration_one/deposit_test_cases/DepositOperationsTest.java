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

public class DepositOperationsTest extends BaseTest {


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
        double balanceBefore = getKateFirstAccountBalance();
        int transactionsBefore = getKateFirstAccountTransactionsCount();

        // Пробую положить на аккаунт невалидную сумму
        DepositRequest request = DepositRequest.builder()
                .id(firstAccountId(USER_KATE))
                .balance(deposit)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .post(request)
                .body(equalTo("Deposit amount must be at least 0.01"));

        // проверяю состояние после запуска теста
        double balanceAfter = getKateFirstAccountBalance();
        int transactionsAfter = getKateFirstAccountTransactionsCount();

        softly.assertThat(balanceAfter).isEqualTo(balanceBefore);
        softly.assertThat(transactionsAfter).isEqualTo(transactionsBefore);
    }

    @Test
    @DisplayName("Проверка невозможности разместить сумму больше 5000 на счёте. 0 < депозит <= 5000")
    public void userCanNotDepositSumAbove5000Test() {
        double balanceBefore = getKateFirstAccountBalance();
        int transactionsBefore = getKateFirstAccountTransactionsCount();

        DepositRequest request = DepositRequest.builder()
                .id(firstAccountId(USER_KATE))
                .balance(5000.01)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .post(request)
                .body(equalTo("Deposit amount cannot exceed 5000"));

        double balanceAfter = getKateFirstAccountBalance();
        int transactionsAfter = getKateFirstAccountTransactionsCount();

        softly.assertThat(balanceAfter).isEqualTo(balanceBefore);
        softly.assertThat(transactionsAfter).isEqualTo(transactionsBefore);
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
        double balanceBefore = accountBefore.getBalance();
        int transactionsBefore = accountBefore.getTransactions().size();

        DepositRequest request = DepositRequest.builder()
                .id(accountId)
                .balance(deposit)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.requestReturnsOK())
                .post(request);

        Account accountAfter = getAccountById(USER_KATE, accountId);

        softly.assertThat(accountAfter.getBalance())
                .as("Balance should increase by " + deposit)
                .isEqualTo(balanceBefore + deposit);

        softly.assertThat(accountAfter.getTransactions().size())
                .as("Transaction count should increase by 1")
                .isEqualTo(transactionsBefore + 1);
    }

    @Test
    @DisplayName("Проверка невозможности разместить депозит на чужом аккаунте")
    public void userCannotDepositToAnotherUsersAccountsTest() {
        double balanceBefore = getKateFirstAccountBalance();
        int transactionsBefore = getKateFirstAccountTransactionsCount();

        DepositRequest request = DepositRequest.builder()
                .id(firstAccountId(USER_ALEX))
                .balance(1)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsForbidden())
                .post(request)
                .body(equalTo("Unauthorized access to account"));

        double balanceAfter = getKateFirstAccountBalance();
        int transactionsAfter = getKateFirstAccountTransactionsCount();

        softly.assertThat(balanceAfter).isEqualTo(balanceBefore);
        softly.assertThat(transactionsAfter).isEqualTo(transactionsBefore);
    }

    @Test
    @DisplayName("Проверка невозможности разместить депозит на несуществующем аккаунте")
    public void cannotDepositToNonExistentAccountTest() {
        double balanceBefore = getKateFirstAccountBalance();
        int transactionsBefore = getKateFirstAccountTransactionsCount();

        DepositRequest request = DepositRequest.builder()
                .id(NON_EXISTENT_ACCOUNT_ID)
                .balance(1)
                .build();

        new DepositRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsForbidden())
                .post(request)
                .body(equalTo("Unauthorized access to account"));

        double balanceAfter = getKateFirstAccountBalance();
        int transactionsAfter = getKateFirstAccountTransactionsCount();

        softly.assertThat(balanceAfter).isEqualTo(balanceBefore);
        softly.assertThat(transactionsAfter).isEqualTo(transactionsBefore);
    }
}
