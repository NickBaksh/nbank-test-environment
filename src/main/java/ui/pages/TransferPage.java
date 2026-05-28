package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferPage extends BasePage<TransferPage> {
    // Заголовок страницы
    private final SelenideElement pageTitle = $(Selectors.byText("\uD83D\uDD04 Make a Transfer"));

    // Поля формы перевода
    private final SelenideElement accountSelector = $(".form-control.account-selector");
    private final SelenideElement recipientNameField = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private final SelenideElement recipientAccountNumberField = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private final SelenideElement amountField = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private final SelenideElement confirmCheckbox = $("#confirmCheck");
    private final SelenideElement transferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));

    // Кнопка "Transfer Again" для перехода к истории
    private final SelenideElement transferAgainButton = $(Selectors.byText("\uD83D\uDD01 Transfer Again"));

    // Элементы страницы истории транзакций
    private final SelenideElement matchingTransactionsTitle = $(Selectors.byText("Matching Transactions"));
    private final SelenideElement searchNameField = $(Selectors.byAttribute("placeholder", "Enter name to find transactions"));
    private final SelenideElement searchButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));

    // Элементы модального окна повторного перевода
    private final SelenideElement modalAccountSelector = $(".modal-body select");
    private final SelenideElement modalAmountField = $(".modal-body input[type='number']");
    private final SelenideElement modalConfirmCheckbox = $("#confirmCheck");
    private final SelenideElement modalTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
    private final SelenideElement repeatTransferTitle = $(Selectors.byText("\uD83D\uDD01 Repeat Transfer"));

    // Константы
    public static final String TRANSFER_PAGE_TITLE = "🔄 Make a Transfer";
    public static final String DEFAULT_ACCOUNT_OPTION = "-- Choose an account --";
    public static final String MATCHING_TRANSACTIONS_TITLE = "Matching Transactions";
    public static final String REPEAT_TRANSFER_TITLE = "🔄 Repeat Transfer";

    public static final String TRANSFER_IN = "TRANSFER_IN";
    public static final String TRANSFER_OUT = "TRANSFER_OUT";

    @Override
    public String url() {
        return "/transfer";
    }

    // ========== Проверки (Assertions) ==========

    public TransferPage shouldHaveTitle(String expectedText) {
        pageTitle.shouldBe(Condition.visible);
        pageTitle.shouldHave(Condition.text(expectedText));
        return this;
    }

    public TransferPage shouldHaveDefaultAccountOption() {
        accountSelector.getSelectedOption().shouldHave(Condition.text(DEFAULT_ACCOUNT_OPTION));
        return this;
    }

    public TransferPage shouldHaveEmptyRecipientName() {
        recipientNameField.shouldBe(Condition.empty);
        return this;
    }

    public TransferPage shouldHaveEmptyRecipientAccount() {
        recipientAccountNumberField.shouldBe(Condition.empty);
        return this;
    }

    public TransferPage shouldHaveEmptyAmount() {
        amountField.shouldBe(Condition.empty);
        return this;
    }

    public TransferPage shouldHaveSelectedAccountValue(String expectedValue) {
        accountSelector.shouldHave(Condition.value(expectedValue));
        return this;
    }

    public TransferPage shouldHaveConfirmCheckboxNotChecked() {
        confirmCheckbox.shouldNotBe(Condition.checked);
        return this;
    }

    // ========== Действия с формой перевода (Actions) ==========

    public TransferPage selectAccount(long accountId) {
        String accountValue = String.valueOf(accountId);
        accountSelector.selectOptionByValue(accountValue);
        shouldHaveSelectedAccountValue(accountValue);
        return this;
    }

    public TransferPage enterRecipientName(String name) {
        recipientNameField.shouldBe(Condition.visible);
        recipientNameField.clear();
        recipientNameField.sendKeys(name);
        return this;
    }

    public TransferPage enterRecipientAccountNumber(long accountId) {
        String accountNumber = "ACC" + accountId;
        recipientAccountNumberField.shouldBe(Condition.visible);
        recipientAccountNumberField.clear();
        recipientAccountNumberField.sendKeys(accountNumber);
        return this;
    }

    public TransferPage enterRecipientAccountNumber(String accountNumber) {
        recipientAccountNumberField.shouldBe(Condition.visible);
        recipientAccountNumberField.clear();
        recipientAccountNumberField.sendKeys(accountNumber);
        return this;
    }

    public TransferPage enterAmount(double amount) {
        amountField.shouldBe(Condition.visible)
                .setValue(String.valueOf(amount));
        return this;
    }

    public TransferPage checkConfirmCheckbox() {
        confirmCheckbox.shouldBe(Condition.visible).click();
        return this;
    }

    public TransferPage clickTransferButton() {
        transferButton.shouldBe(Condition.visible).click();
        return this;
    }

    // ========== Проверка алертов ==========

    public TransferPage verifyAlertAndAccept(String expectedMessage) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).isEqualTo(expectedMessage);
        alert.accept();
        return this;
    }

    public TransferPage verifySuccessfulTransferAlert(double amount, long accountId) {
        String expectedAlert = String.format("✅ Successfully transferred $%s to account ACC%d!",
                amount, accountId);

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).isEqualTo(expectedAlert);

        alert.accept();
        return this;
    }

    public TransferPage verifySuccessfulTransferAlertInModal(double amount, long senderAccountId, long receiveAccountId) {
        String expectedAlert = String.format("✅ Transfer of $%s successful from Account %d to %d!",
                new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP), senderAccountId, receiveAccountId);

        Alert alert = switchTo().alert();
        String alertText = alert.getText();
        assertThat(alertText).isEqualTo(expectedAlert);

        alert.accept();
        return this;
    }

    // ========== Комбинированные действия ==========

    public TransferPage makeTransfer(long fromAccountId, String recipientName, long toAccountId, double amount) {
        return selectAccount(fromAccountId)
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(toAccountId)
                .enterAmount(amount)
                .checkConfirmCheckbox()
                .clickTransferButton();
    }

    // ========== Действия с историей транзакций ==========

    public TransferPage clickTransferAgain() {
        transferAgainButton.shouldBe(Condition.visible).click();
        return this;
    }

    public TransferPage shouldOpenTransactionHistory() {
        matchingTransactionsTitle.shouldBe(Condition.visible);
        return this;
    }

    public TransferPage searchTransactionsByName(String name) {
        searchNameField.shouldBe(Condition.visible);
        searchNameField.clear();
        searchNameField.setValue(name);
        searchButton.shouldBe(Condition.visible).click();
        return this;
    }

    public TransferPage clickRepeatTransferOnFirstTransaction(String transactionType) {
        SelenideElement transactionItem = $$(".list-group-item")
                .findBy(Condition.text(transactionType));
        SelenideElement repeatButton = transactionItem.$(".pink-btn");
        repeatButton.shouldBe(Condition.visible).click();
        return this;
    }

    // ========== Действия с модальным окном повторного перевода ==========

    public TransferPage shouldOpenRepeatTransferModal() {
        repeatTransferTitle.shouldBe(Condition.visible);
        return this;
    }

    public TransferPage selectAccountInModal(long accountId) {
        String accountValue = String.valueOf(accountId);
        modalAccountSelector.selectOptionByValue(accountValue);
        modalAccountSelector.shouldHave(Condition.value(accountValue));
        return this;
    }

    public TransferPage enterAmountInModal(double amount) {
        modalAmountField.shouldBe(Condition.visible)
                .setValue(String.valueOf(amount));
        return this;
    }

    public TransferPage checkConfirmCheckboxInModal() {
        modalConfirmCheckbox.shouldBe(Condition.visible).click();
        return this;
    }

    public TransferPage clickTransferButtonInModal() {
        modalTransferButton.shouldBe(Condition.visible).click();
        return this;
    }

    public TransferPage makeRepeatTransferInModal(long fromAccountId, double amount) {
        return selectAccountInModal(fromAccountId)
                .enterAmountInModal(amount)
                .checkConfirmCheckboxInModal()
                .clickTransferButtonInModal();
    }
}
