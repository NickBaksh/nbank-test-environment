package iteration_one.ui_tests.transaction_test_cases;

import com.codeborne.selenide.*;
import generators.TestUser;
import iteration_one.api_tests.BaseTest;
import models.DepositRequest;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static generators.testdata.ValidTransferAmounts.validTransferAmount;
import static iteration_one.api_tests.profile_name_test_cases.ProfileNameChangingOperationsTest.invalidProfileNames;
import static org.assertj.core.api.Assertions.assertThat;
import static specs.RequestSpecs.BALANCE_5000;

public class TransactionTest extends BaseTest {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.1.103:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options", Map.of(
                "enableVNC", true,
                "enableLog", true
        ));
    }

    @BeforeEach
    public void setUpBalance() {
        long kateAccountId = getKateFirstAccountId();

        // Если баланс уже >= 15000, ничего не делаем
        if (getKateFirstAccountBalance() >= 15000) {
            System.out.println("Balance is sufficient: " + getKateFirstAccountBalance());
            return;
        }

        //Пополняем счёт Кейт на 15000 перед каждым тестом перевода
        //Вызов несколько раз, т.к. есть ограничение на пополнение в 5000
        repeat(3, () -> {
            DepositRequest request = DepositRequest.builder()
                    .id(kateAccountId)
                    .balance(BALANCE_5000)
                    .build();

            new CrudRequester(
                    RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.ACCOUNTS_DEPOSIT)
                    .create(request);
        });

        int maxAttempts = 10;
        double actualBalance = 0;
        for (int i = 0; i < maxAttempts; i++) {
            actualBalance = getKateFirstAccountBalance();
            if (actualBalance >= 15000.00) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        double accountBalanceActual = getKateFirstAccountBalance();
        assertThat(accountBalanceActual).isGreaterThanOrEqualTo(15000.00);
    }

    @Test
    @DisplayName("Перевод валидной суммы между аккаунтами одного клиента")
    public void userCanTransferMoneyTest() {

        // Тестовые данные
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double firstAccountBalanceBeforeTest = getKateFirstAccountBalance();
        double secondAccountBalanceBeforeTest = getKateSecondAccountBalance();
        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        // Создание пользователя и авторизация происходит в BaseTest, в этом шаге получаем значение хедера с токеном авторизации
        String kateToken = getKateToken();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, {profileName}!"

        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + profileName + "!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // Проверка, что на странице отображается текст "🔄 Make a Transfer"
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 4: Выбираем аккаунт
        long accountId = getKateFirstAccountId();
        String senderAccountValue = String.valueOf(accountId);

        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.selectOptionByValue(senderAccountValue);

        // Проверяем, что элемент появился
        accountSelector.shouldHave(Condition.value(senderAccountValue));

        // ШАГ 5: Заполняем имя получателя "Recipient Name:" (тот же клиент)
        SelenideElement recipientNameField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNameField.clear();
        recipientNameField.shouldHave(Condition.empty);

        String recipientName = getCustomerProfileName(TestUser.KATE.getKey());

        // проверяем, что метод вернул не null и передаем его в форму
        Assertions.assertNotNull(recipientName);
        recipientNameField.sendKeys(recipientName);

        // ШАГ 6: Заполняем номер аккаунта получателя "Recipient Account Number:"
        SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccountNumberField.clear();
        recipientAccountNumberField.shouldHave(Condition.empty);

        String recipientAccountNumber = "ACC" + getKateSecondAccountId().toString();
        recipientAccountNumberField.shouldBe(Condition.visible);
        recipientAccountNumberField.sendKeys(recipientAccountNumber);

        // ШАГ 7: Заполняем поле с суммой перевода
        SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
        amountField.shouldBe(Condition.visible);

        double transferAmount = validTransferAmount();
        amountField.setValue(String.valueOf(transferAmount));

        // ШАГ 8: Выбираем чекбокс "Confirm details are correct"
        SelenideElement confirmCheck = $("#confirmCheck");
        confirmCheck.shouldBe(Condition.visible);
        confirmCheck.click();

        // ШАГ 9: Нажимаем на кнопку "🚀 Send Transfer"
        SelenideElement transferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        transferButton.shouldBe(Condition.visible);
        transferButton.click();

        // ШАГ 10: Проверка алерта
        Alert alert = switchTo().alert();
        String successfulTransferAlertText = alert.getText();

        String formattedAmount = String.valueOf(transferAmount);
        long secondAccountId = getKateSecondAccountId();

        softly.assertThat(successfulTransferAlertText)
                .contains("✅ Successfully transferred ")
                .contains("$" + formattedAmount)
                .contains("to account ACC" + secondAccountId);

        alert.accept();


        // ШАГ 11: Проверка через API изменения баланса на аккаунтах
        double firstAccountBalanceActual = getKateFirstAccountBalance();
        double secondAccountBalanceActual = getKateSecondAccountBalance();

        double firstAccountBalanceExpected = firstAccountBalanceBeforeTest - transferAmount;
        double secondAccountBalanceExpected = secondAccountBalanceBeforeTest + transferAmount;

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без выбора аккаунта")
    public void cannotSubmitTransferWithoutAccountTest() {

        // Тестовые данные
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double firstAccountBalanceExpected = getKateFirstAccountBalance();
        double secondAccountBalanceExpected = getKateSecondAccountBalance();
        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        // Создание пользователя и авторизация происходит в BaseTest, в этом шаге получаем значение хедера с токеном авторизации
        String kateToken = getKateToken();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, {profileName}!"

        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + profileName + "!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // Проверка, что на странице отображается текст "🔄 Make a Transfer"
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 4: Проверяем, что поле "Select Your Account:" осталось незаполненным (-- Choose an account --)
        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.getSelectedOption().shouldHave(Condition.text("-- Choose an account --"));

        // ШАГ 5: Заполняем имя получателя "Recipient Name:" (тот же клиент)
        SelenideElement recipientNameField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNameField.clear();
        recipientNameField.shouldHave(Condition.empty);

        String recipientName = getCustomerProfileName(TestUser.KATE.getKey());

        Assertions.assertNotNull(recipientName);
        recipientNameField.sendKeys(recipientName);

        // ШАГ 6: Заполняем номер аккаунта получателя "Recipient Account Number:"
        SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccountNumberField.clear();
        recipientAccountNumberField.shouldHave(Condition.empty);

        String recipientAccountNumber = "ACC" + getKateSecondAccountId().toString();
        recipientAccountNumberField.shouldBe(Condition.visible);
        recipientAccountNumberField.sendKeys(recipientAccountNumber);

        // ШАГ 7: Заполняем поле с суммой перевода
        SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
        amountField.shouldBe(Condition.visible);

        double transferAmount = validTransferAmount();
        amountField.setValue(String.valueOf(transferAmount));

        // ШАГ 8: Выбираем чекбокс "Confirm details are correct"
        SelenideElement confirmCheck = $("#confirmCheck");
        confirmCheck.shouldBe(Condition.visible);
        confirmCheck.click();


        // ШАГ 9: Нажимаем на кнопку "🚀 Send Transfer"
        SelenideElement transferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        transferButton.shouldBe(Condition.visible);
        transferButton.click();

        // ШАГ 10: Проверка алерта
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo("❌ Please fill all fields and confirm.");

        alert.accept();

        // ШАГ 11: Проверка через API изменения баланса на аккаунтах
        double firstAccountBalanceActual = getKateFirstAccountBalance();
        double secondAccountBalanceActual = getKateSecondAccountBalance();

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }


    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без заполнения имени получателя")
    public void cannotSubmitTransferWithoutRecipientNameTest() {

        // Тестовые данные
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double firstAccountBalanceExpected = getKateFirstAccountBalance();
        double secondAccountBalanceExpected = getKateSecondAccountBalance();
        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        // Создание пользователя и авторизация происходит в BaseTest, в этом шаге получаем значение хедера с токеном авторизации
        String kateToken = getKateToken();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, {profileName}!"

        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + profileName + "!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // Проверка, что на странице отображается текст "🔄 Make a Transfer"
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 4: Выбираем аккаунт
        long accountId = getKateFirstAccountId();
        String senderAccountValue = String.valueOf(accountId);

        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.selectOptionByValue(senderAccountValue);

        // ШАГ 5: Проверяем, что поле "Recipient Name:" осталось незаполненным
        SelenideElement recipientNameField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNameField.clear();
        recipientNameField.shouldHave(Condition.empty);

        // ШАГ 6: Заполняем номер аккаунта получателя "Recipient Account Number:"
        SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccountNumberField.clear();
        recipientAccountNumberField.shouldHave(Condition.empty);

        String recipientAccountNumber = "ACC" + getKateSecondAccountId().toString();
        recipientAccountNumberField.shouldBe(Condition.visible);
        recipientAccountNumberField.sendKeys(recipientAccountNumber);

        // ШАГ 7: Заполняем поле с суммой перевода
        SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
        amountField.shouldBe(Condition.visible);

        double transferAmount = validTransferAmount();
        amountField.setValue(String.valueOf(transferAmount));

        // ШАГ 8: Выбираем чекбокс "Confirm details are correct"
        SelenideElement confirmCheck = $("#confirmCheck");
        confirmCheck.shouldBe(Condition.visible);
        confirmCheck.click();


        // ШАГ 9: Нажимаем на кнопку "🚀 Send Transfer"
        SelenideElement transferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        transferButton.shouldBe(Condition.visible);
        transferButton.click();

        // ШАГ 10: Проверка алерта
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo("❌ The recipient name does not match the registered name.");

        alert.accept();

        // ШАГ 11: Проверка через API изменения баланса на аккаунтах
        double firstAccountBalanceActual = getKateFirstAccountBalance();
        double secondAccountBalanceActual = getKateSecondAccountBalance();

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без заполнения номера счета получателя")
    public void cannotSubmitTransferWithoutRecipientAccountTest() {

        // Тестовые данные
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double firstAccountBalanceExpected = getKateFirstAccountBalance();
        double secondAccountBalanceExpected = getKateSecondAccountBalance();
        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        // Создание пользователя и авторизация происходит в BaseTest, в этом шаге получаем значение хедера с токеном авторизации
        String kateToken = getKateToken();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, {profileName}!"

        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + profileName + "!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // Проверка, что на странице отображается текст "🔄 Make a Transfer"
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 4: Выбираем аккаунт
        long accountId = getKateFirstAccountId();
        String senderAccountValue = String.valueOf(accountId);

        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.selectOptionByValue(senderAccountValue);

        // ШАГ 5: Заполняем имя получателя "Recipient Name:" (тот же клиент)
        SelenideElement recipientNameField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNameField.clear();
        recipientNameField.shouldHave(Condition.empty);

        String recipientName = getCustomerProfileName(TestUser.KATE.getKey());

        Assertions.assertNotNull(recipientName);
        recipientNameField.sendKeys(recipientName);

        // ШАГ 6: Проверяем, что поле "Recipient Account Number:" осталось незаполненным
        SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccountNumberField.clear();
        recipientAccountNumberField.shouldHave(Condition.empty);

        // ШАГ 7: Заполняем поле с суммой перевода
        SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
        amountField.shouldBe(Condition.visible);

        double transferAmount = validTransferAmount();
        amountField.setValue(String.valueOf(transferAmount));

        // ШАГ 8: Выбираем чекбокс "Confirm details are correct"
        SelenideElement confirmCheck = $("#confirmCheck");
        confirmCheck.shouldBe(Condition.visible);
        confirmCheck.click();


        // ШАГ 9: Нажимаем на кнопку "🚀 Send Transfer"
        SelenideElement transferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        transferButton.shouldBe(Condition.visible);
        transferButton.click();

        // ШАГ 10: Проверка алерта
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo("❌ Please fill all fields and confirm.");

        alert.accept();

        // ШАГ 11: Проверка через API изменения баланса на аккаунтах
        double firstAccountBalanceActual = getKateFirstAccountBalance();
        double secondAccountBalanceActual = getKateSecondAccountBalance();

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }


    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без заполнения суммы перевода")
    public void cannotSubmitTransferWithoutAmountTest() {

        // Тестовые данные
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double firstAccountBalanceExpected = getKateFirstAccountBalance();
        double secondAccountBalanceExpected = getKateSecondAccountBalance();
        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        // Создание пользователя и авторизация происходит в BaseTest, в этом шаге получаем значение хедера с токеном авторизации
        String kateToken = getKateToken();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, {profileName}!"

        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + profileName + "!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // Проверка, что на странице отображается текст "🔄 Make a Transfer"
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 4: Выбираем аккаунт
        long accountId = getKateFirstAccountId();
        String senderAccountValue = String.valueOf(accountId);

        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.selectOptionByValue(senderAccountValue);

        // ШАГ 5: Заполняем имя получателя "Recipient Name:" (тот же клиент)
        SelenideElement recipientNameField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNameField.clear();
        recipientNameField.shouldHave(Condition.empty);

        String recipientName = getCustomerProfileName(TestUser.KATE.getKey());

        Assertions.assertNotNull(recipientName);
        recipientNameField.sendKeys(recipientName);

        // ШАГ 6: Заполняем номер аккаунта получателя "Recipient Account Number:"
        SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccountNumberField.clear();
        recipientAccountNumberField.shouldHave(Condition.empty);

        String recipientAccountNumber = "ACC" + getKateSecondAccountId().toString();
        recipientAccountNumberField.shouldBe(Condition.visible);
        recipientAccountNumberField.sendKeys(recipientAccountNumber);

        // ШАГ 7: Проверяем, что поле "Amount:" осталось пустым
        SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
        amountField.shouldBe(Condition.visible);
        amountField.clear();
        amountField.shouldBe(Condition.empty);

        // ШАГ 8: Выбираем чекбокс "Confirm details are correct"
        SelenideElement confirmCheck = $("#confirmCheck");
        confirmCheck.shouldBe(Condition.visible);
        confirmCheck.click();

        // ШАГ 9: Нажимаем на кнопку "🚀 Send Transfer"
        SelenideElement transferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        transferButton.shouldBe(Condition.visible);
        transferButton.click();

        // ШАГ 10: Проверка алерта
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo("❌ Please fill all fields and confirm.");

        alert.accept();

        // ШАГ 11: Проверка через API изменения баланса на аккаунтах
        double firstAccountBalanceActual = getKateFirstAccountBalance();
        double secondAccountBalanceActual = getKateSecondAccountBalance();

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму трансфера без подтверждения правильности данных")
    public void cannotSubmitTransferWithoutConfirmCheckboxTest() {

        // Тестовые данные
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double firstAccountBalanceExpected = getKateFirstAccountBalance();
        double secondAccountBalanceExpected = getKateSecondAccountBalance();
        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        // Создание пользователя и авторизация происходит в BaseTest, в этом шаге получаем значение хедера с токеном авторизации
        String kateToken = getKateToken();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, {profileName}!"

        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + profileName + "!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // Проверка, что на странице отображается текст "🔄 Make a Transfer"
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 4: Выбираем аккаунт
        long accountId = getKateFirstAccountId();
        String senderAccountValue = String.valueOf(accountId);

        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.selectOptionByValue(senderAccountValue);

        // ШАГ 5: Заполняем имя получателя "Recipient Name:" (тот же клиент)
        SelenideElement recipientNameField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
        recipientNameField.clear();
        recipientNameField.shouldHave(Condition.empty);

        String recipientName = getCustomerProfileName(TestUser.KATE.getKey());

        Assertions.assertNotNull(recipientName);
        recipientNameField.sendKeys(recipientName);

        // ШАГ 6: Заполняем номер аккаунта получателя "Recipient Account Number:"
        SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
        recipientAccountNumberField.clear();
        recipientAccountNumberField.shouldHave(Condition.empty);

        String recipientAccountNumber = "ACC" + getKateSecondAccountId().toString();
        recipientAccountNumberField.shouldBe(Condition.visible);
        recipientAccountNumberField.sendKeys(recipientAccountNumber);

        // ШАГ 7: Заполняем поле с суммой перевода
        SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
        amountField.shouldBe(Condition.visible);

        double transferAmount = validTransferAmount();
        amountField.setValue(String.valueOf(transferAmount));

        // ШАГ 8: Проверяем, что чекбокс "Confirm details are correct" не выбран
        SelenideElement confirmCheck = $("#confirmCheck");
        confirmCheck.shouldBe(Condition.visible);
        confirmCheck.shouldNotBe(Condition.checked);

        // ШАГ 9: Нажимаем на кнопку "🚀 Send Transfer"
        SelenideElement transferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        transferButton.shouldBe(Condition.visible);
        transferButton.click();

        // ШАГ 10: Проверка алерта
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo("❌ Please fill all fields and confirm.");

        alert.accept();

        // ШАГ 11: Проверка через API изменения баланса на аккаунтах
        double firstAccountBalanceActual = getKateFirstAccountBalance();
        double secondAccountBalanceActual = getKateSecondAccountBalance();

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь может совершить повторный перевод между своими аккаунтами")
    public void repeatedTransfersBetweenAccountsWorkCorrectlyTest() {

        // Тестовые данные
        String username = getCustomerUsername(TestUser.KATE.getKey());
        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
        transferMoneyFromFirstToSecondUserAccount();
        double firstAccountBalanceExpected = getKateFirstAccountBalance();
        double secondAccountBalanceExpected = getKateSecondAccountBalance();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        // Создание пользователя и авторизация происходит в BaseTest, в этом шаге получаем значение хедера с токеном авторизации
        String kateToken = getKateToken();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, {profileName}!"

        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + profileName + "!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // Проверка, что на странице отображается текст "🔄 Make a Transfer"
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 4: Нажимаем на кнопку "🔁 Transfer Again"
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));
        transferAgainButton.shouldBe(Condition.visible);
        transferAgainButton.click();


        // Проверяем, что перешли на странице с историей транзакций
        $(Selectors.byText("Matching Transactions")).shouldBe(Condition.visible);

        // ШАГ 5: Ищем транзакцию по имени пользователя
        SelenideElement nameField = $(Selectors.byAttribute("placeholder", "Enter name to find transactions"));
        nameField.clear();
        nameField.shouldHave(Condition.empty);

        nameField.setValue(profileName);

        // Нажимаем на кнопку "🔍 Search Transactions"
        SelenideElement searchButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));
        searchButton.shouldBe(Condition.visible);
        searchButton.click();

        // ШАГ 6: Выбираем первую транзакцию с отправкой суммы (TRANSFER_IN)
        SelenideElement transferOutItem = $$(".list-group-item")
                .findBy(Condition.text("TRANSFER_IN"));

        SelenideElement repeatButton = transferOutItem.$(".pink-btn");
        repeatButton.shouldBe(Condition.visible);
        repeatButton.click();

        $(Selectors.byText("\uD83D\uDD01 Repeat Transfer")).shouldBe(Condition.visible);

        // ШАГ 7: Выбираем аккаунт
        long accountId = getKateFirstAccountId();
        String senderAccountValue = String.valueOf(accountId);

        SelenideElement accountSelector = $(".modal-body select");
        accountSelector.selectOptionByValue(senderAccountValue);

        accountSelector.shouldHave(Condition.value(senderAccountValue));

        String selectedAccountValue = accountSelector.getValue();
        System.out.println("Selected account in modal: " + selectedAccountValue);
        softly.assertThat(selectedAccountValue).isEqualTo(senderAccountValue);

        // ШАГ 8: Заполняем поле с суммой перевода
        SelenideElement amountField = $(".modal-body input[type='number']");
        amountField.shouldBe(Condition.visible);

        double transferAmount = validTransferAmount();
        amountField.setValue(String.valueOf(transferAmount));

        System.out.println("=== Modal content debug ===");
        String modalText = $(".modal-body").getText();
        System.out.println(modalText);

        // ШАГ 9: Выбираем чекбокс "Confirm details are correct"
        SelenideElement confirmCheck = $("#confirmCheck");
        confirmCheck.shouldBe(Condition.visible);
        confirmCheck.click();

        // ШАГ 10: Нажимаем на кнопку "🚀 Send Transfer"
        SelenideElement transferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
        transferButton.shouldBe(Condition.visible);
        transferButton.click();

        // ШАГ 11: Проверяем алерт
        Alert alert = switchTo().alert();
        String successfulTransferAlertText = alert.getText();
        System.out.println(successfulTransferAlertText);

        String formattedAmount = String.valueOf(transferAmount);
        String firstAccountId = String.valueOf(getKateFirstAccountId());
        String secondAccountId = String.valueOf(getKateSecondAccountId());


        // Возможно тут баг в форме, если выбрать TRANSFER_OUT перевод, то при переводе на ->
        // -> Confirm transfer to Account ID: 597, переводится с счета 598 на 598
        softly.assertThat(successfulTransferAlertText)
                .contains("✅ Transfer of ")
                .contains("$" + formattedAmount)
                .contains(" successful from Account ")
                .contains(firstAccountId)
                .contains(" to ")
                .contains(secondAccountId)
                .contains("!");

        alert.accept();

        // ШАГ 12: Проверка через API изменения баланса на аккаунтах
        double firstAccountBalanceActual = getKateFirstAccountBalance();
        double secondAccountBalanceActual = getKateSecondAccountBalance();

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected - transferAmount);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected + transferAmount);
    }

    @Test
    @DisplayName("Поиск несуществующего пользователя в истории транзакций не показывает результатов")
    public void searchNonExistentUserShowsNoResultsTest() {

        // Тестовые данные
        String username = getCustomerUsername(TestUser.KATE.getKey());
        String profileName = updateProfileNameToValidRandomValue(TestUser.KATE);
        transferMoneyFromFirstToSecondUserAccount();
        double firstAccountBalanceExpected = getKateFirstAccountBalance();
        double secondAccountBalanceExpected = getKateSecondAccountBalance();
        String invalidName = invalidProfileNames().findFirst().orElseThrow();


        // ШАГ 1: Авторизоваться под учетной записью пользователя
        // Создание пользователя и авторизация происходит в BaseTest, в этом шаге получаем значение хедера с токеном авторизации
        String kateToken = getKateToken();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, {profileName}!"

        $(Selectors.byClassName("welcome-text"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, " + profileName + "!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        // Проверка, что на странице отображается текст "🔄 Make a Transfer"
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).shouldBe(Condition.visible);

        // ШАГ 4: Нажимаем на кнопку "🔁 Transfer Again"
        SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));
        transferAgainButton.shouldBe(Condition.visible);
        transferAgainButton.click();


        // Проверяем, что перешли на странице с историей транзакций
        $(Selectors.byText("Matching Transactions")).shouldBe(Condition.visible);

        // ШАГ 5: Получаем ошибку при попытке найти транзакции клиента которого не существует
        SelenideElement nameField = $(Selectors.byAttribute("placeholder", "Enter name to find transactions"));
        nameField.clear();
        nameField.shouldHave(Condition.empty);

        nameField.setValue(invalidName);

        // Нажимаем на кнопку "🔍 Search Transactions"
        SelenideElement searchButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));
        searchButton.shouldBe(Condition.visible);
        searchButton.click();

        // ШАГ 6: Проверяем алерт
        Alert alert = switchTo().alert();
        String noMatchingUsersAlertText = alert.getText();

        softly.assertThat(noMatchingUsersAlertText).isEqualTo("❌ No matching users found.");

        alert.accept();

        // ШАГ 7: Проверка через API изменения баланса на аккаунтах
        double firstAccountBalanceActual = getKateFirstAccountBalance();
        double secondAccountBalanceActual = getKateSecondAccountBalance();

        softly.assertThat(firstAccountBalanceActual).isEqualTo(firstAccountBalanceExpected);
        softly.assertThat(secondAccountBalanceActual).isEqualTo(secondAccountBalanceExpected);
    }
}
