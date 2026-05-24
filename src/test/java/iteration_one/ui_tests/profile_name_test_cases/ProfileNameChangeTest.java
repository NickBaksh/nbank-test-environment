package iteration_one.ui_tests.profile_name_test_cases;

import com.codeborne.selenide.*;
import generators.TestUser;
import iteration_one.api_tests.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;

import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static iteration_one.api_tests.profile_name_test_cases.ProfileNameChangingOperationsTest.invalidProfileNames;
import static iteration_one.api_tests.profile_name_test_cases.ProfileNameChangingOperationsTest.validProfileNames;

public class ProfileNameChangeTest extends BaseTest {

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

    @Test
    @DisplayName("Проверка изменения имени профиля на валидное значение (два слова состоящие только из букв)")
    public void userCanChangeProfileNameTest() {
        // ШАГ 1: Авторизоваться под учетной записью пользователя
        String kateToken = getKateToken();
        String username = getCustomerUsername(TestUser.KATE.getKey());

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, noname!"
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу редактирования имени профиля
        $(".user-username").click();

        // Проверка, что на странице отображается текст "✏️ Edit Profile"
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);


        // Похоже тут есть гонка данных и строка сначала заполняется текстом, затем .clear() чистит строку
        // Удаляем любые значения в строке если они были
        SelenideElement nameField = $(Selectors.byAttribute("placeholder", "Enter new name"));
        nameField.clear();
        nameField.shouldHave(Condition.empty);

        Selenide.screenshot("before_submit_empty_name");
        // ШАГ 4: Генерируем валидное имя и записываем его в строку
        String validName = validProfileNames().findFirst().orElseThrow();
        //Thread.sleep(500);

        nameField.sendKeys(validName);

        // ШАГ 5: Нажимаем на кнопку "💾 Save Changes"
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).shouldBe(Condition.visible).click();

        // ШАГ 6: Проверка, что алерт "✅ Name updated successfully!"
        Alert alert = switchTo().alert();

        softly.assertThat(alert.getText()).isEqualTo("✅ Name updated successfully!");

        alert.accept();

        // ШАГ 7: Проверить, что имя профиля обновилось на сервере
        String profileNameAfterTest = getCustomerProfileName(TestUser.KATE.getKey());
        softly.assertThat(profileNameAfterTest).isEqualTo(validName);
    }


    @Test
    @DisplayName("Проверка UI валидации при отправке некорректного имени профиля")
    public void userCannotSaveEmptyProfileNameTest() {
        // ШАГ 1: Авторизоваться под учетной записью пользователя
        String kateToken = getKateToken();
        String username = getCustomerUsername(TestUser.KATE.getKey());
        String profileNameExpected = getCustomerProfileName(TestUser.KATE.getKey());

        // ШАГ 2: Перейти на страницу "User Dashboard"
        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", kateToken);
        Selenide.open("/dashboard");

        // Проверка, что открылась верная страница с текстом "Welcome, noname!"
        $(Selectors.byClassName("welcome-text")).shouldBe(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));

        // Проверка, что на странице отображается username пользователя под которым авторизовались в системе
        String usernameUI = $(".user-username").getText().replace("@", "");

        softly.assertThat(usernameUI).isEqualTo(username);

        // ШАГ 3: Переход на страницу редактирования имени профиля
        $(".user-username").click();

        // Проверка, что на странице отображается текст "✏️ Edit Profile"
        $(Selectors.byText("✏\uFE0F Edit Profile")).shouldBe(Condition.visible);


        // Удаляем любые значения в строке если они были
        SelenideElement nameField = $(Selectors.byAttribute("placeholder", "Enter new name"));
        nameField.clear();
        nameField.setValue("");
        nameField.shouldHave(Condition.empty);

        // ШАГ 4: Генерируем некорректное имя и записываем его в строку
        String invalidName = invalidProfileNames().findFirst().orElseThrow();
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(invalidName);

        // ШАГ 5: Нажимаем на кнопку "💾 Save Changes"
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).shouldBe(Condition.visible).click();

        // ШАГ 6: Проверка, что алерт "Name must contain two words with letters only"
        Alert alert = switchTo().alert();

        softly.assertThat(alert.getText()).isEqualTo("Name must contain two words with letters only");

        alert.accept();

        // ШАГ 7: Проверить, что имя профиля обновилось на сервере
        String profileNameActual = getCustomerProfileName(TestUser.KATE.getKey());
        softly.assertThat(profileNameActual).isEqualTo(profileNameExpected);
    }
}
