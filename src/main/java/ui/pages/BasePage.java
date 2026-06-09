package ui.pages;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverConditions;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));
    protected SelenideElement button = $("button");

    // ========== Методы авторизации ==========

    /**
     * Авторизация через токен (для UI тестов)
     */
    public static void authWithToken(String token) {
        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", token);
    }

    /**
     * Авторизация через логин и пароль
     */
    public static void authWithCredentials(String username, String password) {
        Selenide.open("/");
        String token = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", token);
    }

    /**
     * Авторизация через CreateUserRequest (устаревший метод, будет удалён)
     *
     * @deprecated Используйте {@link #authWithToken(String)} или {@link #authWithCredentials(String, String)}
     */
    @Deprecated
    public static void authAsUser(CreateUserRequest userRequest) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(userRequest.getUsername(), userRequest.getPassword());
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    // ========== Основные методы страницы ==========

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T shouldHaveUrl(String expectedUrl) {
        Selenide.webdriver().shouldHave(WebDriverConditions.url(expectedUrl));
        return (T) this;
    }

    public T checkAlertMessageAndAccept(BankAlert bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert.getMessage());
        alert.accept();
        return (T) this;
    }
}