//package iteration_one.ui_tests.transaction_test_cases;
//
//import api.generators.TestUser;
//import iteration_one.ui_tests.BaseUiTest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import ui.pages.UserDashboard;
//
//import static api.generators.testdata.ValidTransferAmounts.validTransferAmount;
//import static ui.pages.BankAlert.*;
//import static ui.pages.TransferPage.TRANSFER_IN;
//import static ui.pages.TransferPage.TRANSFER_PAGE_TITLE;
//
//public class TransactionTest extends BaseUiTest {
//
//    @BeforeEach
//    public void setUpBalance() {
//        ensureKateBalance();
//    }
//
//    @Test
//    @DisplayName("Перевод валидной суммы между аккаунтами одного клиента")
//    public void userCanTransferMoneyTest() {
//
//        // Тестовые данные
//        String username = getCustomerUsername(TestUser.KATE.getKey());
//        double firstAccountBalanceBeforeTest = getKateFirstAccountBalance();
//        double secondAccountBalanceBeforeTest = getKateSecondAccountBalance();
//
//        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
//        long senderAccountId = getKateFirstAccountId();
//        long recipientAccountId = getKateSecondAccountId();
//        double transferAmount = validTransferAmount();
//
//
//        // ШАГ 1: Авторизоваться под учетной записью пользователя
//        authAsUser(TestUser.KATE);
//
//        // ШАГ 2: Выполнить шаги теста
//        new UserDashboard()
//                .open()
//                .shouldHaveWelcomeTextForProfile(profileName)
//                .shouldHaveUsername(username)
//                .goToTransferPage()
//                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
//                .makeTransfer(senderAccountId, profileName, recipientAccountId, transferAmount)
//                .verifySuccessfulTransferAlert(transferAmount, recipientAccountId);
//
//        // ШАГ 3: Проверить, что балансы аккаунтов соответствуют ожидаемым
//        double firstAccountBalanceActual = getKateFirstAccountBalance();
//        double secondAccountBalanceActual = getKateSecondAccountBalance();
//
//        double firstAccountBalanceExpected = firstAccountBalanceBeforeTest - transferAmount;
//        double secondAccountBalanceExpected = secondAccountBalanceBeforeTest + transferAmount;
//
//        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
//        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
//    }
//
//    @Test
//    @DisplayName("Пользователь не может отправить форму трансфера без выбора аккаунта")
//    public void cannotSubmitTransferWithoutAccountTest() {
//
//        // Тестовые данные
//        String username = getCustomerUsername(TestUser.KATE.getKey());
//        double firstAccountBalanceExpected = getKateFirstAccountBalance();
//        double secondAccountBalanceExpected = getKateSecondAccountBalance();
//
//        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
//        long recipientAccountId = getKateSecondAccountId();
//        double transferAmount = validTransferAmount();
//
//
//        // ШАГ 1: Авторизоваться под учетной записью пользователя
//        authAsUser(TestUser.KATE);
//
//        // ШАГ 2: Выполнить шаги теста
//        new UserDashboard()
//                .open()
//                .shouldHaveWelcomeTextForProfile(profileName)
//                .shouldHaveUsername(username)
//                .goToTransferPage()
//                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
//                .shouldHaveDefaultAccountOption()
//                .enterRecipientName(profileName)
//                .enterRecipientAccountNumber(recipientAccountId)
//                .enterAmount(transferAmount)
//                .checkConfirmCheckbox()
//                .clickTransferButton()
//                .verifyAlertAndAccept(PLEASE_FILL_ALL_FIELDS.getMessage());
//
//        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
//        double firstAccountBalanceActual = getKateFirstAccountBalance();
//        double secondAccountBalanceActual = getKateSecondAccountBalance();
//
//        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
//        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
//    }
//
//
//    @Test
//    @DisplayName("Пользователь не может отправить форму трансфера без заполнения имени получателя")
//    public void cannotSubmitTransferWithoutRecipientNameTest() {
//
//        // Тестовые данные
//        String username = getCustomerUsername(TestUser.KATE.getKey());
//        double firstAccountBalanceExpected = getKateFirstAccountBalance();
//        double secondAccountBalanceExpected = getKateSecondAccountBalance();
//
//        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
//        long senderAccountId = getKateFirstAccountId();
//        long recipientAccountId = getKateSecondAccountId();
//        double transferAmount = validTransferAmount();
//
//
//        // ШАГ 1: Авторизоваться под учетной записью пользователя
//        authAsUser(TestUser.KATE);
//
//        // ШАГ 2: Выполнить шаги теста
//        new UserDashboard()
//                .open()
//                .shouldHaveWelcomeTextForProfile(profileName)
//                .shouldHaveUsername(username)
//                .goToTransferPage()
//                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
//                .selectAccount(senderAccountId)
//                .shouldHaveEmptyRecipientName()
//                .enterRecipientAccountNumber(recipientAccountId)
//                .enterAmount(transferAmount)
//                .checkConfirmCheckbox()
//                .clickTransferButton()
//                .verifyAlertAndAccept(THE_RECIPIENT_NAME_DOES_NOT_MATCH.getMessage());
//
//        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
//        double firstAccountBalanceActual = getKateFirstAccountBalance();
//        double secondAccountBalanceActual = getKateSecondAccountBalance();
//
//        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
//        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
//    }
//
//    @Test
//    @DisplayName("Пользователь не может отправить форму трансфера без заполнения номера счета получателя")
//    public void cannotSubmitTransferWithoutRecipientAccountTest() {
//
//        // Тестовые данные
//        String username = getCustomerUsername(TestUser.KATE.getKey());
//        double firstAccountBalanceExpected = getKateFirstAccountBalance();
//        double secondAccountBalanceExpected = getKateSecondAccountBalance();
//
//        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
//        long senderAccountId = getKateFirstAccountId();
//        double transferAmount = validTransferAmount();
//
//
//        // ШАГ 1: Авторизоваться под учетной записью пользователя
//        authAsUser(TestUser.KATE);
//
//        // ШАГ 2: Выполнить шаги теста
//        new UserDashboard()
//                .open()
//                .shouldHaveWelcomeTextForProfile(profileName)
//                .shouldHaveUsername(username)
//                .goToTransferPage()
//                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
//                .selectAccount(senderAccountId)
//                .enterRecipientName(profileName)
//                .shouldHaveEmptyRecipientAccount()
//                .enterAmount(transferAmount)
//                .checkConfirmCheckbox()
//                .clickTransferButton()
//                .verifyAlertAndAccept(PLEASE_FILL_ALL_FIELDS.getMessage());
//
//        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
//        double firstAccountBalanceActual = getKateFirstAccountBalance();
//        double secondAccountBalanceActual = getKateSecondAccountBalance();
//
//        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
//        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
//    }
//
//
//    @Test
//    @DisplayName("Пользователь не может отправить форму трансфера без заполнения суммы перевода")
//    public void cannotSubmitTransferWithoutAmountTest() {
//
//        // Тестовые данные
//        String username = getCustomerUsername(TestUser.KATE.getKey());
//        double firstAccountBalanceExpected = getKateFirstAccountBalance();
//        double secondAccountBalanceExpected = getKateSecondAccountBalance();
//
//        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
//        long senderAccountId = getKateFirstAccountId();
//        long recipientAccountId = getKateSecondAccountId();
//
//
//        // ШАГ 1: Авторизоваться под учетной записью пользователя
//        authAsUser(TestUser.KATE);
//
//        // ШАГ 2: Выполнить шаги теста
//        new UserDashboard()
//                .open()
//                .shouldHaveWelcomeTextForProfile(profileName)
//                .shouldHaveUsername(username)
//                .goToTransferPage()
//                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
//                .selectAccount(senderAccountId)
//                .enterRecipientName(profileName)
//                .enterRecipientAccountNumber(recipientAccountId)
//                .shouldHaveEmptyAmount()
//                .checkConfirmCheckbox()
//                .clickTransferButton()
//                .verifyAlertAndAccept(PLEASE_FILL_ALL_FIELDS.getMessage());
//
//        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
//        double firstAccountBalanceActual = getKateFirstAccountBalance();
//        double secondAccountBalanceActual = getKateSecondAccountBalance();
//
//        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
//        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
//    }
//
//    @Test
//    @DisplayName("Пользователь не может отправить форму трансфера без подтверждения правильности данных")
//    public void cannotSubmitTransferWithoutConfirmCheckboxTest() {
//
//        // Тестовые данные
//        String username = getCustomerUsername(TestUser.KATE.getKey());
//        double firstAccountBalanceExpected = getKateFirstAccountBalance();
//        double secondAccountBalanceExpected = getKateSecondAccountBalance();
//
//        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
//        long senderAccountId = getKateFirstAccountId();
//        long recipientAccountId = getKateSecondAccountId();
//        double transferAmount = validTransferAmount();
//
//
//        // ШАГ 1: Авторизоваться под учетной записью пользователя
//        authAsUser(TestUser.KATE);
//
//        // ШАГ 2: Выполнить шаги теста
//        new UserDashboard()
//                .open()
//                .shouldHaveWelcomeTextForProfile(profileName)
//                .shouldHaveUsername(username)
//                .goToTransferPage()
//                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
//                .selectAccount(senderAccountId)
//                .enterRecipientName(profileName)
//                .enterRecipientAccountNumber(recipientAccountId)
//                .enterAmount(transferAmount)
//                .shouldHaveConfirmCheckboxNotChecked()
//                .clickTransferButton()
//                .verifyAlertAndAccept(PLEASE_FILL_ALL_FIELDS.getMessage());
//
//        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
//        double firstAccountBalanceActual = getKateFirstAccountBalance();
//        double secondAccountBalanceActual = getKateSecondAccountBalance();
//
//        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
//        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
//    }
//
//    @Test
//    @DisplayName("Пользователь может совершить повторный перевод между своими аккаунтами")
//    public void repeatedTransfersBetweenAccountsWorkCorrectlyTest() {
//
//        // Тестовые данные
//        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
//        long senderAccountId = getKateFirstAccountId();
//        long receiverAccountId = getKateSecondAccountId();
//        double modalTransferAmount = validTransferAmount();
//        transferMoneyFromFirstToSecondUserAccount();
//
//        String username = getCustomerUsername(TestUser.KATE.getKey());
//        double firstAccountBalanceExpected = getKateFirstAccountBalance() - modalTransferAmount;
//        double secondAccountBalanceExpected = getKateSecondAccountBalance() + modalTransferAmount;
//
//        // ШАГ 1: Авторизоваться под учетной записью пользователя
//        authAsUser(TestUser.KATE);
//
//        // ШАГ 2: Выполнить шаги теста
//        new UserDashboard()
//                .open()
//                .shouldHaveWelcomeTextForProfile(profileName)
//                .shouldHaveUsername(username)
//                .goToTransferPage()
//                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
//                .clickTransferAgain()
//                .shouldOpenTransactionHistory()
//                .searchTransactionsByName(profileName)
//                .clickRepeatTransferOnFirstTransaction(TRANSFER_IN)
//                .selectAccountInModal(senderAccountId)
//                .enterAmountInModal(modalTransferAmount)
//                .checkConfirmCheckboxInModal()
//                .clickTransferButtonInModal()
//                .verifySuccessfulTransferAlertInModal(modalTransferAmount, senderAccountId, receiverAccountId);
//
//        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
//        double firstAccountBalanceActual = getKateFirstAccountBalance();
//        double secondAccountBalanceActual = getKateSecondAccountBalance();
//
//        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
//        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
//    }
//
//    @Test
//    @DisplayName("Поиск несуществующего пользователя в истории транзакций не показывает результатов")
//    public void searchNonExistentUserShowsNoResultsTest() {
//
//        // Тестовые данные
//        String username = getCustomerUsername(TestUser.KATE.getKey());
//        double firstAccountBalanceExpected = getKateFirstAccountBalance();
//        double secondAccountBalanceExpected = getKateSecondAccountBalance();
//
//        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
//        String invalidName = invalidProfileName();
//
//        // ШАГ 1: Авторизоваться под учетной записью пользователя
//        authAsUser(TestUser.KATE);
//
//        // ШАГ 2: Выполнить шаги теста
//        new UserDashboard()
//                .open()
//                .shouldHaveWelcomeTextForProfile(profileName)
//                .shouldHaveUsername(username)
//                .goToTransferPage()
//                .shouldHaveTitle(TRANSFER_PAGE_TITLE)
//                .clickTransferAgain()
//                .shouldOpenTransactionHistory()
//                .searchTransactionsByName(invalidName)
//                .verifyAlertAndAccept(NO_MATCHING_USERS_FOUND.getMessage());
//
//        // ШАГ 3: Проверить, что балансы аккаунтов не изменились
//        double firstAccountBalanceActual = getKateFirstAccountBalance();
//        double secondAccountBalanceActual = getKateSecondAccountBalance();
//
//        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
//        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
//    }
//}
