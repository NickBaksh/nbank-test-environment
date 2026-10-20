package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.By;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {
    private static final By WELCOME_LOCATOR = By.className("welcome-text");
    // Шаблон приветствия
    private static final String WELCOME_TEXT_PATTERN = "Welcome, %s!";
    private SelenideElement welcomeTextElement = $(Selectors.byClassName("welcome-text"));
    private SelenideElement usernameElement = $(".user-username");
    private SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement transferButton = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));

    // Получить ожидаемый текст приветствия по имени профиля
    private static String getWelcomeText(String profileName) {
        return String.format(WELCOME_TEXT_PATTERN, profileName);
    }

    public SelenideElement getWelcomeTextElement() {
        // Обычный Selenide элемент
        return $(WELCOME_LOCATOR);
    }

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard shouldHaveWelcomeText(String expectedText) {
        welcomeTextElement.shouldBe(Condition.visible).shouldHave(Condition.text(expectedText));
        return this;
    }

    public UserDashboard shouldHaveWelcomeTextForProfile(String profileName) {
//        Selenide.sleep(500);
        String expectedText = getWelcomeText(profileName);
        welcomeTextElement.shouldBe(Condition.visible)
                .shouldHave(Condition.text(expectedText), Duration.ofSeconds(10));
        return this;
    }

    public UserDashboard shouldHaveUsername(String expectedUsername) {
        String actual = usernameElement.getText().replace("@", "");
        assertThat(actual).isEqualTo(expectedUsername);
        return this;
    }

    public EditProfilePage goToEditProfile() {
        usernameElement.click();
        return new EditProfilePage();
    }

    public DepositPage goToDepositMoneyPage() {
        depositButton.click();
        return new DepositPage();
    }

    public TransferPage goToTransferPage() {
        transferButton.click();
        return new TransferPage();
    }
}
