package ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverConditions;
import org.openqa.selenium.Alert;

import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
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

    public Object checkAlertMessageAndAccept(BankAlert bankAlert) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(bankAlert.getMessage());
        alert.accept();
        return (T) this;
    }
}
