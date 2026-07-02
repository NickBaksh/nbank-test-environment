package iteration_one.ui_tests.deposit_test_cases;

import com.codeborne.selenide.*;
import generators.TestUser;
import generators.testdata.InvalidDepositCase;
import iteration_one.api_tests.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.*;
import static generators.testdata.ValidDepositAmounts.validDepositAmount;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseTest {

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

    static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(InvalidDepositCase.NEGATIVE.generate(), "❌ Please enter a valid amount."),      // отрицательная
                Arguments.of(InvalidDepositCase.ZERO.generate(), "❌ Please enter a valid amount."),         // ноль
                Arguments.of(InvalidDepositCase.ABOVE_MAX.generate(), "❌ Please deposit less or equal to 5000$.")        // больше 5000
        );
    }

    @Test
    @DisplayName("Пополнение счета через UI на валидную сумму")
    public void userCanDepositMoneyTest() {

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        String kateToken = getKateToken();
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceBeforeTest = getKateFirstAccountBalance();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, noname!"
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        // Проверка, что на странице отображается текст "💰 Deposit Money"
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 4: Выбираем аккаунт
        long accountId = getKateFirstAccountId();
        String accountValue = String.valueOf(accountId);

        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.selectOptionByValue(accountValue);

        // Проверяем, что элемент появился
        accountSelector.shouldHave(Condition.value(accountValue));

        // ШАГ 5: Вводим сумму депозита
        SelenideElement depositInput = $(".form-control.deposit-input");

        Double depositAmount = validDepositAmount();
        BigDecimal roundedAmount = new BigDecimal(depositAmount).setScale(2, RoundingMode.HALF_UP);

        depositInput.setValue(String.valueOf(roundedAmount));

        //ШАГ 6: Нажимаем на кнопку "💵 Deposit"
        SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        depositButton.shouldBe(Condition.visible);
        depositButton.click();

        // ШАГ 7: Проверка, что алерт "✅ Successfully deposited $5000 to account ACC298!"
        Alert alert = switchTo().alert();
        String successfulDepositAlertText = alert.getText();

        String formattedAmount = String.valueOf(depositAmount);

        softly.assertThat(successfulDepositAlertText)
                .contains("✅ Successfully deposited")
                .contains("$" + formattedAmount)
                .contains("to account ACC" + accountId);

        alert.accept();

        // ШАГ 8: Проверить через API, что корректная сумма депозита оказалась на счёте
        double accountBalanceActual = getKateFirstAccountBalance();
        double accountBalanceExpected = accountBalanceBeforeTest + depositAmount;
        softly.assertThat(accountBalanceActual).isEqualTo(accountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму депозита без выбора аккаунта")
    public void cannotDepositWithoutAccountTest() {

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        String kateToken = getKateToken();
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceExpected = getKateFirstAccountBalance();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, noname!"
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        // Проверка, что на странице отображается текст "💰 Deposit Money"
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 4: Проверяем, что поле "Select Account:" осталось пустым
        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.getSelectedOption().shouldHave(Condition.text("-- Choose an account --"));

        // ШАГ 5: Вводим сумму депозита
        SelenideElement depositInput = $(".form-control.deposit-input");

        Double depositAmount = validDepositAmount();
        //BigDecimal roundedAmount = new BigDecimal(depositAmount).setScale(2, RoundingMode.HALF_UP);

        depositInput.setValue(String.valueOf(depositAmount));

        //ШАГ 6: Нажимаем на кнопку "💵 Deposit"
        SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        depositButton.shouldBe(Condition.visible);
        depositButton.click();

        // ШАГ 7: Проверка, что алерт "❌ Please select an account."
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo("❌ Please select an account.");

        // ШАГ 8: Проверить через API, что корректная сумма депозита оказалась на счёте
        double accountBalanceActual = getKateFirstAccountBalance();
        softly.assertThat(accountBalanceActual).isEqualTo(accountBalanceExpected);
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAmounts")
    @DisplayName("Пользователь не может отправить форму депозита с невалидной суммой (меньше 0.01 и больше 5000)")
    public void cannotSubmitDepositWithInvalidAmountTest(double invalidAmount, String expectedAlert) {

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        String kateToken = getKateToken();
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceExpected = getKateFirstAccountBalance();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, noname!"
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        // Проверка, что на странице отображается текст "💰 Deposit Money"
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 4: Выбираем аккаунт
        long accountId = getKateFirstAccountId();
        String accountValue = String.valueOf(accountId);

        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.selectOptionByValue(accountValue);

        // Проверяем, что элемент появился
        accountSelector.shouldHave(Condition.value(accountValue));

        // ШАГ 5: Вводим невалидную сумму депозита
        SelenideElement depositInput = $(".form-control.deposit-input");
        depositInput.setValue(String.valueOf(invalidAmount));

        //ШАГ 6: Нажимаем на кнопку "💵 Deposit"
        SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        depositButton.shouldBe(Condition.visible);
        depositButton.click();

        // ШАГ 7: Проверка алерта
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo(expectedAlert);

        // ШАГ 8: Проверить через API, что корректная сумма депозита оказалась на счёте
        double accountBalanceActual = getKateFirstAccountBalance();
        softly.assertThat(accountBalanceActual).isEqualTo(accountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму депозита без выбора аккаунта")
    public void cannotDepositWithoutAccountAndDepositSumTest() {

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        String kateToken = getKateToken();
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceExpected = getKateFirstAccountBalance();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, noname!"
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        // Проверка, что на странице отображается текст "💰 Deposit Money"
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 4: Проверяем, что поле "Select Account:" осталось пустым
        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.getSelectedOption().shouldHave(Condition.text("-- Choose an account --"));

        // ШАГ 5: Вводим сумму депозита
        SelenideElement depositInput = $(".form-control.deposit-input");

        Double depositAmount = validDepositAmount();
        BigDecimal roundedAmount = new BigDecimal(depositAmount).setScale(2, RoundingMode.HALF_UP);

        depositInput.setValue(String.valueOf(roundedAmount));

        //ШАГ 6: Нажимаем на кнопку "💵 Deposit"
        SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        depositButton.shouldBe(Condition.visible);
        depositButton.click();

        // ШАГ 7: Проверка, что алерт "❌ Please select an account."
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo("❌ Please select an account.");

        // ШАГ 8: Проверить через API, что корректная сумма депозита оказалась на счёте
        double accountBalanceActual = getKateFirstAccountBalance();
        softly.assertThat(accountBalanceActual).isEqualTo(accountBalanceExpected);
    }

    @Test
    @DisplayName("Пользователь не может отправить форму депозита с пустыми полями")
    public void cannotSubmitDepositWithEmptyAccountAndAmountTest() {

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        String kateToken = getKateToken();
        String username = getCustomerUsername(TestUser.KATE.getKey());
        double accountBalanceExpected = getKateFirstAccountBalance();

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, noname!"
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу депозита
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).click();

        // Проверка, что на странице отображается текст "💰 Deposit Money"
        $(Selectors.byText("\uD83D\uDCB0 Deposit Money")).shouldBe(Condition.visible);

        // ШАГ 4: Проверяем, что поле "Select Account:" осталось пустым
        SelenideElement accountSelector = $(".form-control.account-selector");
        accountSelector.getSelectedOption().shouldHave(Condition.text("-- Choose an account --"));

        // ШАГ 5: Проверяем, что поле "Enter Amount:" осталось пустым
        SelenideElement depositInput = $(".form-control.deposit-input");
        depositInput.shouldBe(Condition.empty);

        //ШАГ 6: Нажимаем на кнопку "💵 Deposit"
        SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));
        depositButton.shouldBe(Condition.visible);
        depositButton.click();

        // ШАГ 7: Проверка, что алерт "❌ Please select an account."
        Alert alert = switchTo().alert();
        softly.assertThat(alert.getText()).isEqualTo("❌ Please select an account.");

        // ШАГ 8: Проверить через API, что корректная сумма депозита оказалась на счёте
        double accountBalanceActual = getKateFirstAccountBalance();
        softly.assertThat(accountBalanceActual).isEqualTo(accountBalanceExpected);
    }
}
