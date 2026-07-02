package iteration_one.ui_tests.transaction_test_cases;

import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import common.annotations.Browsers;
import common.annotations.Environments;
import common.annotations.UserSession;
import iteration_one.ui_tests.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.UserDashboard;

import static api.generators.testdata.ValidTransferAmounts.validTransferAmount;
import static api.requests.steps.UserSteps.transferMoneyFromFirstToSecondUserAccount;
import static org.assertj.core.api.Assertions.within;
import static ui.pages.BankAlert.*;
import static ui.pages.BasePage.authWithToken;
import static ui.pages.TransferPage.TRANSFER_PAGE_TITLE;

public class TransactionTest extends BaseUiTest {

    @Test
    @DisplayName("Перевод валидной суммы с одного аккаунта на другой")
    @UserSession
    @Environments
    @Browsers
    public void userCanTransferMoneyTest() {

        // Тестовые данные
        TestUserContext user = getCurrentUser();

        UserSteps.setUpBalance(user);

        String token = user.getToken();
        String username = user.getUsername();
        double firstAccountBalanceBeforeTest = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceBeforeTest = UserSteps.getSecondAccountBalance(user);

        String profileName = UserSteps.updateProfileNameToRandomName(user);
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();
        String secondAccountNumber = user.getAccountNumber(secondAccountId);
        double transferAmount = validTransferAmount();


        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeTextForProfile(profileName)
                .shouldHaveUsername(username)
                .goToTransferPage()
                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
                .makeTransfer(firstAccountId, profileName, secondAccountNumber, transferAmount)
                .verifySuccessfulTransferAlert(transferAmount, secondAccountNumber);

        // ШАГ 3: Проверить, что балансы аккаунтов соответствуют ожидаемым
        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);

        double firstAccountBalanceExpected = firstAccountBalanceBeforeTest - transferAmount;
        double secondAccountBalanceExpected = secondAccountBalanceBeforeTest + transferAmount;

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без выбора аккаунта")
    @UserSession
    @Environments
    @Browsers
    public void cannotSubmitTransferWithoutAccountTest() {

        // Тестовые данные
        TestUserContext user = getCurrentUser();

        UserSteps.setUpBalance(user);

        String token = user.getToken();
        String username = user.getUsername();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);

        String profileName = UserSteps.updateProfileNameToRandomName(user);
        long secondAccountId = user.getSecondAccountId();
        String secondAccountNumber = user.getAccountNumber(secondAccountId);
        double transferAmount = validTransferAmount();


        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeTextForProfile(profileName)
                .shouldHaveUsername(username)
                .goToTransferPage()
                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
                .shouldHaveDefaultAccountOption()
                .enterRecipientName(profileName)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transferAmount)
                .checkConfirmCheckbox()
                .clickTransferButton()
                .verifyAlertAndAccept(PLEASE_FILL_ALL_FIELDS.getMessage());

        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }


    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без заполнения имени получателя")
    @UserSession
    @Environments
    @Browsers
    public void cannotSubmitTransferWithoutRecipientNameTest() {

        // Тестовые данные
        TestUserContext user = getCurrentUser();

        UserSteps.setUpBalance(user);

        String token = user.getToken();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);

        String profileName = UserSteps.updateProfileNameToRandomName(user);
        String username = user.getUsername();
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();
        String secondAccountNumber = user.getAccountNumber(secondAccountId);
        double transferAmount = validTransferAmount();


        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeTextForProfile(profileName)
                .shouldHaveUsername(username)
                .goToTransferPage()
                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
                .selectAccount(firstAccountId)
                .shouldHaveEmptyRecipientName()
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transferAmount)
                .checkConfirmCheckbox()
                .clickTransferButtonAndVerifyAlertAndAccept(THE_RECIPIENT_NAME_DOES_NOT_MATCH.getMessage());

        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без заполнения номера счета получателя")
    @UserSession
    @Environments
    @Browsers
    public void cannotSubmitTransferWithoutRecipientAccountTest() {

        // Тестовые данные
        TestUserContext user = getCurrentUser();

        UserSteps.setUpBalance(user);

        String token = user.getToken();
        String username = user.getUsername();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);

        String profileName = UserSteps.updateProfileNameToRandomName(user);
        long firstAccountId = user.getFirstAccountId();
        double transferAmount = validTransferAmount();


        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeTextForProfile(profileName)
                .shouldHaveUsername(username)
                .goToTransferPage()
                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
                .selectAccount(firstAccountId)
                .enterRecipientName(profileName)
                .shouldHaveEmptyRecipientAccount()
                .enterAmount(transferAmount)
                .checkConfirmCheckbox()
                .clickTransferButton()
                .verifyAlertAndAccept(PLEASE_FILL_ALL_FIELDS.getMessage());

        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }


    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без заполнения суммы перевода")
    @UserSession
    @Environments
    @Browsers
    public void cannotSubmitTransferWithoutAmountTest() {

        // Тестовые данные
        TestUserContext user = getCurrentUser();

        UserSteps.setUpBalance(user);

        String token = user.getToken();
        String username = user.getUsername();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);

        String profileName = UserSteps.updateProfileNameToRandomName(user);
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();
        String secondAccountNumber = user.getAccountNumber(secondAccountId);


        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeTextForProfile(profileName)
                .shouldHaveUsername(username)
                .goToTransferPage()
                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
                .selectAccount(firstAccountId)
                .enterRecipientName(profileName)
                .enterRecipientAccountNumber(secondAccountNumber)
                .shouldHaveEmptyAmount()
                .checkConfirmCheckbox()
                .clickTransferButton()
                .verifyAlertAndAccept(PLEASE_FILL_ALL_FIELDS.getMessage());

        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без подтверждения правильности данных")
    @UserSession
    @Environments
    @Browsers
    public void cannotSubmitTransferWithoutConfirmCheckboxTest() {

        // Тестовые данные
        TestUserContext user = getCurrentUser();

        UserSteps.setUpBalance(user);

        String token = user.getToken();
        String username = user.getUsername();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);

        String profileName = UserSteps.updateProfileNameToRandomName(user);
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();
        String secondAccountNumber = user.getAccountNumber(secondAccountId);
        double transferAmount = validTransferAmount();


        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeTextForProfile(profileName)
                .shouldHaveUsername(username)
                .goToTransferPage()
                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
                .selectAccount(firstAccountId)
                .enterRecipientName(profileName)
                .enterRecipientAccountNumber(secondAccountNumber)
                .enterAmount(transferAmount)
                .shouldHaveConfirmCheckboxNotChecked()
                .clickTransferButton()
                .verifyAlertAndAccept(PLEASE_FILL_ALL_FIELDS.getMessage());

        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь может совершить повторный перевод между своими аккаунтами")
    @UserSession
    @Environments
    @Browsers
    public void repeatedTransfersBetweenAccountsWorkCorrectlyTest() {

        // Тестовые данные
        TestUserContext user = getCurrentUser();

        UserSteps.setUpBalance(user);
        transferMoneyFromFirstToSecondUserAccount(user);

        String token = user.getToken();
        String username = user.getUsername();

        String profileName = UserSteps.updateProfileNameToRandomName(user);
        long firstAccountId = user.getFirstAccountId();
        long secondAccountId = user.getSecondAccountId();
        double transferAmount = validTransferAmount();

        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user) - transferAmount;
        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user) + transferAmount;

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeTextForProfile(profileName)
                .shouldHaveUsername(username)
                .goToTransferPage()
                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
                .clickTransferAgain()
                .shouldOpenTransactionHistory()
                .inputName(profileName)
                .clickSearchByNameButton()
                .clickRepeatOnFirstTransferIn()
                .selectAccountInModal(firstAccountId)
                .enterAmountInModal(transferAmount)
                .checkConfirmCheckboxInModal()
                .clickTransferButtonInModal()
                .verifySuccessfulTransferAlertInModal(transferAmount, firstAccountId, secondAccountId);

        // ШАГ 3: Проверить, что балансы аккаунтов изменились
        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);

        softly.assertThat(firstAccountBalanceActual)
                .isCloseTo(firstAccountBalanceExpected, within(0.01));
        softly.assertThat(secondAccountBalanceActual)
                .isCloseTo(secondAccountBalanceExpected, within(0.01));
    }

    @Test
    @DisplayName("Поиск несуществующего пользователя в истории транзакций не показывает результатов")
    @UserSession
    @Environments
    @Browsers
    public void searchNonExistentUserShowsNoResultsTest() {

        // Тестовые данные
        TestUserContext user = getCurrentUser();

        UserSteps.setUpBalance(user);

        String token = user.getToken();
        String username = user.getUsername();
        double firstAccountBalanceExpected = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceExpected = UserSteps.getSecondAccountBalance(user);

        String profileName = UserSteps.updateProfileNameToRandomName(user);
        String invalidName = UserSteps.generateInvalidProfileName();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeTextForProfile(profileName)
                .shouldHaveUsername(username)
                .goToTransferPage()
                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
                .clickTransferAgain()
                .shouldOpenTransactionHistory()
                .inputName(invalidName)
                .clickSearchButtonAndVerifyAlertAndAccept(NO_MATCHING_USERS_FOUND.getMessage());

        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
        double firstAccountBalanceActual = UserSteps.getFirstAccountBalance(user);
        double secondAccountBalanceActual = UserSteps.getSecondAccountBalance(user);

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }
}
