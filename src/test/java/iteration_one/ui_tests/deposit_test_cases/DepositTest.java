package iteration_one.ui_tests.deposit_test_cases;

import api.generators.TestUser;
import api.generators.testdata.InvalidDepositCase;
import iteration_one.ui_tests.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.UserDashboard;

import java.util.stream.Stream;

import static api.generators.testdata.ValidDepositAmounts.validDepositAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static ui.pages.BankAlert.*;

public class DepositTest extends BaseUiTest {

    static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(InvalidDepositCase.NEGATIVE.generate(), PLEASE_ENTER_A_VALID_AMOUNT.getMessage()),      // отрицательная
                Arguments.of(InvalidDepositCase.ZERO.generate(), PLEASE_ENTER_A_VALID_AMOUNT.getMessage()),         // ноль
                Arguments.of(InvalidDepositCase.ABOVE_MAX.generate(), PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000.getMessage())        // больше 5000
        );
    }

    @Test
    @DisplayName("Пополнение счета через UI на валидную сумму")
    public void userCanDepositMoneyTest() {
        // Данные для теста
        String expectedUsername = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceBefore = getKateFirstAccountBalance();
        long accountId = getKateFirstAccountId();
        Double depositAmount = validDepositAmount();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authAsUser(TestUser.KATE);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToDepositMoneyPage()
                .shouldHaveTitle(DEPOSIT_MONEY_TEXT)
                .makeDeposit(accountId, depositAmount)
                .verifySuccessfulDepositAlert(depositAmount, accountId);

        // ШАГ 3: Проверить, что счёт клиента пополнился на сумму депозита
        double accountBalanceAfter = getKateFirstAccountBalance();
        double expectedBalance = accountBalanceBefore + depositAmount;
        assertThat(accountBalanceAfter).isEqualTo(expectedBalance);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму депозита без выбора аккаунта")
    public void cannotDepositWithoutAccountTest() {

        // Данные для теста
        String expectedUsername = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceBefore = getKateFirstAccountBalance();
        Double depositAmount = validDepositAmount();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authAsUser(TestUser.KATE);

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
        double accountBalanceAfter = getKateFirstAccountBalance();
        assertThat(accountBalanceAfter).isEqualTo(accountBalanceBefore);
    }


    @Test
    @DisplayName("Пользователь не может отправить форму депозита без суммы")
    public void cannotDepositWithoutDepositAmountTest() {

        // Данные для теста
        String expectedUsername = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceBefore = getKateFirstAccountBalance();
        long accountId = getKateFirstAccountId();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authAsUser(TestUser.KATE);

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
        double accountBalanceAfter = getKateFirstAccountBalance();
        assertThat(accountBalanceAfter).isEqualTo(accountBalanceBefore);
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAmounts")
    @DisplayName("Пользователь не может отправить форму депозита с невалидной суммой (меньше 0.01 и больше 5000)")
    public void cannotSubmitDepositWithInvalidAmountTest(double invalidAmount, String expectedAlert) {

        // Данные для теста
        String expectedUsername = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceBefore = getKateFirstAccountBalance();
        long accountId = getKateFirstAccountId();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authAsUser(TestUser.KATE);

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
        double accountBalanceAfter = getKateFirstAccountBalance();
        assertThat(accountBalanceAfter).isEqualTo(accountBalanceBefore);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму депозита с пустыми полями")
    public void cannotDepositWithoutAccountAndDepositSumTest() {

        // Данные для теста
        String expectedUsername = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceBefore = getKateFirstAccountBalance();
        long accountId = getKateFirstAccountId();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authAsUser(TestUser.KATE);

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
        double accountBalanceAfter = getKateFirstAccountBalance();
        assertThat(accountBalanceAfter).isEqualTo(accountBalanceBefore);
    }
}
