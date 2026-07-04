package iteration_one.ui_tests.deposit_test_cases;

import api.models.dao_model.AccountDao;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import common.annotations.ApiVersion;
import common.annotations.Browsers;
import common.annotations.Environments;
import common.annotations.UserSession;
import db.*;
import iteration_one.ui_tests.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.UserDashboard;

import java.util.List;

import static api.generators.testdata.ValidDepositAmounts.validDepositAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static ui.pages.BankAlert.PLEASE_ENTER_A_VALID_AMOUNT;
import static ui.pages.BankAlert.PLEASE_SELECT_AN_ACCOUNT;
import static ui.pages.BasePage.authWithToken;

public class DepositTest extends BaseUiTest {

    @Test
    @UserSession
    @Environments
    @Browsers
    @DisplayName("Пополнение счета через UI на валидную сумму. Тест для API V2")
    @ApiVersion("v2")
    public void userCanDepositMoneyApiV2Test() {
        // Данные для теста
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        String expectedUsername = user.getUsername();
        double accountBalanceBefore = UserSteps.getFirstAccountBalance(user);
        long accountId = user.getFirstAccountId();
        double depositAmount = validDepositAmount();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToDepositMoneyPage()
                .shouldHaveTitle(DEPOSIT_MONEY_TEXT)
                .makeDeposit(accountId, depositAmount)
                .verifySuccessfulDepositByAccountId(depositAmount, accountId);

        // ШАГ 3: Проверить, что счёт клиента пополнился на сумму депозита
        double accountBalanceAfter = UserSteps.getFirstAccountBalance(user);
        double expectedBalance = accountBalanceBefore + depositAmount;
        assertThat(accountBalanceAfter).isEqualTo(expectedBalance);
    }

    @Test
    @UserSession
    @Environments
    @Browsers
    @DisplayName("Пополнение счета через UI на валидную сумму. Для версии API V1")
    public void userCanDepositMoneyApiV1Test() {
        // Данные для теста
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        String expectedUsername = user.getUsername();
        double accountBalanceBefore = UserSteps.getFirstAccountBalance(user);
        long accountId = user.getFirstAccountId();
        String accountNumber = user.getAccountNumber(accountId);
        double depositAmount = validDepositAmount();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToDepositMoneyPage()
                .shouldHaveTitle(DEPOSIT_MONEY_TEXT)
                .makeDeposit(accountId, depositAmount)
                .verifySuccessfulDepositByAccountNumber(depositAmount, accountNumber);

        // SELECT запрос
        List<AccountDao> accounts = DBRequest.<AccountDao>builder()
                .requestType(RequestType.SELECT)
                .table(Tables.ACCOUNTS.getTableName())
                .where(Condition.equalTo("account_number", accountNumber))
                .extractAs(AccountDao.class)
                .build()
                .execute(AccountDao.rowMapper);

        // ✅ Логируем результат
        System.out.println("📊 DB Query Result:");
        System.out.println("   Account number: " + accountNumber);
        System.out.println("   Records found: " + accounts.size());

        if (!accounts.isEmpty()) {
            AccountDao account = accounts.get(0);
            System.out.println("   Account ID: " + account.getId());
            System.out.println("   Balance: " + account.getBalance());
            System.out.println("   Customer ID: " + account.getCustomerId());
        } else {
            System.out.println("   ❌ No records found!");
        }

        // ШАГ 3: Проверить, что счёт клиента пополнился на сумму депозита
        double accountBalanceAfter = UserSteps.getFirstAccountBalance(user);
        double expectedBalance = accountBalanceBefore + depositAmount;
        assertThat(accountBalanceAfter).isEqualTo(expectedBalance);
    }

    @Test
    @UserSession
    @Environments
    @Browsers
    @DisplayName("Пользователь не может отправить форму депозита без выбора аккаунта")
    public void cannotDepositWithoutAccountTest() {
        // Данные для теста
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        String expectedUsername = user.getUsername();
        double accountBalanceBefore = UserSteps.getFirstAccountBalance(user);
        double depositAmount = validDepositAmount();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToDepositMoneyPage()
                .shouldHaveTitle(DEPOSIT_MONEY_TEXT)
                .shouldHaveDefaultAccountOption()
                .enterAmount(depositAmount)
                .clickDepositButton()
                .verifyAlertAndAccept(PLEASE_SELECT_AN_ACCOUNT.getMessage());

        // ШАГ 3: Проверить, что сумма на счёте клиента не изменилась
        double accountBalanceAfter = UserSteps.getFirstAccountBalance(user);
        ;
        assertThat(accountBalanceAfter).isEqualTo(accountBalanceBefore);
    }


    @Test
    @UserSession
    @Environments
    @Browsers
    @DisplayName("Пользователь не может отправить форму депозита без суммы")

    public void cannotDepositWithoutDepositAmountTest() {

        // Данные для теста
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        String expectedUsername = user.getUsername();
        double accountBalanceBefore = UserSteps.getFirstAccountBalance(user);
        long accountId = user.getFirstAccountId();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToDepositMoneyPage()
                .shouldHaveTitle(DEPOSIT_MONEY_TEXT)
                .selectAccount(accountId)
                .shouldHaveEmptyAmountField()
                .clickDepositButton()
                .verifyAlertAndAccept(PLEASE_ENTER_A_VALID_AMOUNT.getMessage());

        // ШАГ 3: Проверить, что сумма на счёте клиента не изменилась
        double accountBalanceAfter = UserSteps.getFirstAccountBalance(user);
        assertThat(accountBalanceAfter).isEqualTo(accountBalanceBefore);
    }

    @ParameterizedTest
    @UserSession
    @Environments
    @Browsers
    @MethodSource("invalidDepositAmountsUi")
    @DisplayName("Пользователь не может отправить форму депозита с невалидной суммой (меньше 0.01 и больше 5000)")
    public void cannotSubmitDepositWithInvalidAmountTest(double invalidAmount, String expectedAlert) {
        // Данные для теста
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        String expectedUsername = user.getUsername();
        double accountBalanceBefore = UserSteps.getFirstAccountBalance(user);
        long accountId = user.getFirstAccountId();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToDepositMoneyPage()
                .shouldHaveTitle(DEPOSIT_MONEY_TEXT)
                .selectAccount(accountId)
                .enterAmount(invalidAmount)
                .clickDepositButton()
                .verifyAlertAndAccept(expectedAlert);

        // ШАГ 3: Проверить, что сумма на счёте клиента не изменилась
        double accountBalanceAfter = UserSteps.getFirstAccountBalance(user);
        assertThat(accountBalanceAfter).isEqualTo(accountBalanceBefore);
    }

    @Test
    @UserSession
    @Environments
    @Browsers
    @DisplayName("Пользователь не может отправить форму депозита с пустыми полями")
    public void cannotDepositWithoutAccountAndDepositSumTest() {
        // Данные для теста
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        String expectedUsername = user.getUsername();
        double accountBalanceBefore = UserSteps.getFirstAccountBalance(user);
        long accountId = user.getFirstAccountId();
        String accountNumber = user.getAccountNumber(accountId);

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToDepositMoneyPage()
                .shouldHaveTitle(DEPOSIT_MONEY_TEXT)
                .shouldHaveDefaultAccountOption()
                .shouldHaveEmptyAmountField()
                .clickDepositButton()
                .verifyAlertAndAccept(PLEASE_SELECT_AN_ACCOUNT.getMessage());


        // ШАГ 3: Проверить, что сумма на счёте клиента не изменилась
        double accountBalanceAfter = UserSteps.getFirstAccountBalance(user);
        assertThat(accountBalanceAfter).isEqualTo(accountBalanceBefore);
    }
}
