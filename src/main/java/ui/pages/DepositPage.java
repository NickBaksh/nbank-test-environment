package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositPage extends BasePage<DepositPage> {
    public static SelenideElement depositTitle = $(Selectors.byText("\uD83D\uDCB0 Deposit Money"));
    private SelenideElement accountSelector = $(".form-control.account-selector");
    private SelenideElement depositInput = $(".form-control.deposit-input");
    private SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));


    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage shouldHaveTitle(String expectedText) {
        depositTitle.shouldBe(Condition.visible);
        depositTitle.shouldHave(Condition.text(expectedText));
        return this;
    }

    public DepositPage shouldHaveDefaultAccountOption() {
        accountSelector.getSelectedOption().shouldHave(Condition.text("-- Choose an account --"));
        return this;
    }

    public DepositPage shouldHaveEmptyAmountField() {
        depositInput.shouldBe(Condition.empty);
        return this;
    }

    public DepositPage shouldHaveSelectedAccountValue(String expectedValue) {
        accountSelector.shouldHave(Condition.value(expectedValue));
        return this;
    }

    // Действия (actions)
    public DepositPage selectAccount(long accountId) {
        String accountValue = String.valueOf(accountId);
        accountSelector.selectOptionByValue(accountValue);
        shouldHaveSelectedAccountValue(accountValue);
        return this;
    }

    public DepositPage enterAmount(double amount) {
        depositInput.setValue(String.valueOf(amount));
        return this;
    }

    public DepositPage enterAmount(String amount) {
        depositInput.setValue(amount);
        return this;
    }

    public DepositPage clickDepositButton() {
        depositButton.shouldBe(Condition.visible).click();
        return this;
    }

    // Работа с алертами
    public DepositPage verifySuccessfulDepositAlert(double amount, long accountId) {
        String expectedAlert = String.format("✅ Successfully deposited $%s to account ACC%d!",
                new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP),
                accountId);

        Alert alert = switchTo().alert();
        String actualText = alert.getText();
        assertThat(actualText).isEqualTo(expectedAlert);
        alert.accept();

        return this;
    }

    public DepositPage verifyAlertAndAccept(String expectedMessage) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).isEqualTo(expectedMessage);
        alert.accept();
        return this;
    }

    public String getAlertTextAndAccept() {
        Alert alert = switchTo().alert();
        String text = alert.getText();
        alert.accept();
        return text;
    }

    public DepositPage verifyAlertContains(String expectedPart) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).contains(expectedPart);
        alert.accept();
        return this;
    }

    // Комбинированные действия
    public DepositPage makeDeposit(long accountId, double amount) {
        return selectAccount(accountId)
                .enterAmount(amount)
                .clickDepositButton();
    }
}
