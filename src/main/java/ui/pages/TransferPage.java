package ui.pages;

import com.codeborne.selenide.*;
import common.helpers.StepLogger;
import ui.elements.OperationsList;
import utils.AlertHelper;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class TransferPage extends BasePage<TransferPage> {
    // Константы
    public static final String TRANSFER_PAGE_TITLE = "🔄 Make a Transfer";
    public static final String DEFAULT_ACCOUNT_OPTION = "-- Choose an account --";
    public static final String MATCHING_TRANSACTIONS_TITLE = "Matching Transactions";
    public static final String TRANSACTION_HISTORY_TITLE = "Transaction History";
    public static final String REPEAT_TRANSFER_TITLE = "🔄 Repeat Transfer";
    public static final String TRANSFER_IN = "TRANSFER_IN";
    public static final String TRANSFER_OUT = "TRANSFER_OUT";
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
    // Заголовок истории операций выбранного счёта
    private final SelenideElement transactionHistoryTitle = $(Selectors.byText(TRANSACTION_HISTORY_TITLE));
    // Поиск транзакций по имени (в preprod-версии фронтенда отсутствует)
    private final SelenideElement matchingTransactionsTitle = $(Selectors.byText(MATCHING_TRANSACTIONS_TITLE));
    private final SelenideElement searchNameField = $(Selectors.byAttribute("placeholder", "Enter name to find transactions"));
    private final SelenideElement searchButton = $(Selectors.byText("\uD83D\uDD0D Search Transactions"));
    // Элементы модального окна повторного перевода
    private final SelenideElement modalAccountSelector = $(".modal-body select");
    private final SelenideElement modalAmountField = $(".modal-body input[type='number']");
    private final SelenideElement modalConfirmCheckbox = $("#confirmCheck");
    private final SelenideElement modalTransferButton = $(Selectors.byText("\uD83D\uDE80 Send Transfer"));
    private final SelenideElement repeatTransferTitle = $(Selectors.byText("\uD83D\uDD01 Repeat Transfer"));

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

    // ========== Действия с формой перевода (RequestType) ==========

    public TransferPage selectAccount(long accountId) {
        return StepLogger.logWithScreenshot("Select sender account " + accountId, () -> {
            String accountValue = String.valueOf(accountId);
            accountSelector.shouldBe(Condition.visible).selectOptionByValue(accountValue);
            shouldHaveSelectedAccountValue(accountValue);
            return this;
        });
    }

    public TransferPage enterRecipientName(String name) {
        return StepLogger.logWithScreenshot("Enter recipient name '" + name + "'", () -> {
            recipientNameField.shouldBe(Condition.visible);
            recipientNameField.clear();
            recipientNameField.sendKeys(name);
            return this;
        });
    }

    public TransferPage enterRecipientAccountNumber(long accountId) {
        return StepLogger.logWithScreenshot("Enter recipient account number ACC" + accountId, () -> {
            String accountNumber = "ACC" + accountId;
            recipientAccountNumberField.shouldBe(Condition.visible);
            recipientAccountNumberField.clear();
            recipientAccountNumberField.sendKeys(accountNumber);
            return this;
        });
    }

    public TransferPage enterRecipientAccountNumber(String accountNumber) {
        return StepLogger.logWithScreenshot("Enter recipient account number '" + accountNumber + "'", () -> {
            recipientAccountNumberField.shouldBe(Condition.visible);
            recipientAccountNumberField.clear();
            recipientAccountNumberField.sendKeys(accountNumber);
            return this;
        });
    }

    public TransferPage enterAmount(double amount) {
        return StepLogger.logWithScreenshot("Enter amount " + amount, () -> {
            amountField.shouldBe(Condition.visible)
                    .setValue(String.valueOf(amount));
            return this;
        });
    }

    public TransferPage checkConfirmCheckbox() {
        return StepLogger.logWithScreenshot("Check confirm checkbox", () -> {
            confirmCheckbox.shouldBe(Condition.visible).click();
            return this;
        });
    }

    public TransferPage clickTransferButton() {
        return StepLogger.logWithScreenshot("Click Send Transfer button", () -> {
            transferButton.shouldBe(Condition.visible).shouldBe(Condition.enabled).click();
            return this;
        });
    }

    // ========== Проверка алертов ==========

    public TransferPage verifyAlertAndAccept(String expectedMessage) {
        Selenide.confirm(expectedMessage);
        return this;
    }

    public TransferPage clickTransferButtonAndVerifyAlertAndAccept(String expectedMessage) {
        return StepLogger.logWithScreenshot("Click Send Transfer and verify alert '" + expectedMessage + "'", () -> {
            AlertHelper.clickAndVerifyAlert(transferButton, expectedMessage);
            return this;
        });
    }

    public TransferPage clickSearchButtonAndVerifyAlertAndAccept(String expectedMessage) {
        return StepLogger.logWithScreenshot("Click Search and verify alert '" + expectedMessage + "'", () -> {
            AlertHelper.clickAndVerifyAlert(searchButton, expectedMessage);
            return this;
        });
    }

    public TransferPage verifySuccessfulTransferAlert(double amount, String accountNumber) {
        String expectedAlert = String.format("✅ Successfully transferred $%s to account %s!",
                amount, accountNumber);

        Selenide.confirm(expectedAlert);
        return this;
    }

    public TransferPage verifySuccessfulTransferAlertInModal(double amount, long senderAccountId, long receiveAccountId) {
        String expectedAlert = String.format("✅ Transfer of $%s successful from Account %d to %d!",
                amount, senderAccountId, receiveAccountId);
        Selenide.confirm(expectedAlert);
        return this;
    }


    // ========== Комбинированные действия ==========

    // Делаем перевод в котором accountNumber == ACC + accountId
    public TransferPage makeTransfer(long fromAccountId, String recipientName, long toAccountId, double amount) {
        return StepLogger.log(String.format("Make transfer %s from account %d to ACC%d (recipient '%s')",
                amount, fromAccountId, toAccountId, recipientName), () -> selectAccount(fromAccountId)
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(toAccountId)
                .enterAmount(amount)
                .checkConfirmCheckbox()
                .clickTransferButton());
    }

    // Делаем перевод в котором accountNumber генерируется в новом формате
    public TransferPage makeTransfer(long fromAccountId, String recipientName, String toAccountNumber, double amount) {
        return StepLogger.log(String.format("Make transfer %s from account %d to %s (recipient '%s')",
                amount, fromAccountId, toAccountNumber, recipientName), () -> selectAccount(fromAccountId)
                .enterRecipientName(recipientName)
                .enterRecipientAccountNumber(toAccountNumber)
                .enterAmount(amount)
                .checkConfirmCheckbox()
                .clickTransferButton());
    }

    // ========== Действия с историей транзакций ==========

    public TransferPage clickTransferAgain() {
        return StepLogger.logWithScreenshot("Click Transfer Again", () -> {
            transferAgainButton
                    .shouldBe(Condition.visible)
                    .shouldBe(Condition.enabled)
                    .click();
            return this;
        });
    }

    public TransferPage shouldOpenTransactionHistory() {
        transactionHistoryTitle.shouldBe(Condition.visible);
        return this;
    }

    public TransferPage shouldOpenMatchingTransactions() {
        matchingTransactionsTitle.shouldBe(Condition.visible);
        return this;
    }

    public TransferPage inputName(String name) {
        return StepLogger.logWithScreenshot("Enter search name '" + name + "'", () -> {
            searchNameField.shouldBe(Condition.visible);
            searchNameField.shouldBe(Condition.enabled);
            searchNameField.clear();
            searchNameField.setValue(name);
            searchNameField.shouldHave(Condition.value(name));
            return this;
        });
    }

    public TransferPage clickSearchByNameButton() {
        return StepLogger.logWithScreenshot("Click Search by name button", () -> {
            searchButton.shouldBe(Condition.visible);
            searchButton.shouldBe(Condition.enabled);
            searchButton.click();
            return this;
        });
    }

    public TransferPage clickRepeatTransferOnFirstTransaction(String transactionType) {
        return StepLogger.logWithScreenshot("Click Repeat on first " + transactionType + " transaction", () -> {
            SelenideElement transactionItem = $$(".list-group-item")
                    .findBy(Condition.text(transactionType));
            SelenideElement repeatButton = transactionItem.$(".pink-btn");
            repeatButton.shouldBe(Condition.visible).click();
            return this;
        });
    }

    // ========== Действия с модальным окном повторного перевода ==========

    public TransferPage shouldOpenRepeatTransferModal() {
        repeatTransferTitle.shouldBe(Condition.visible);
        return this;
    }

    public TransferPage selectAccountInModal(long accountId) {
        return StepLogger.logWithScreenshot("Select sender account " + accountId + " in modal", () -> {
            String accountValue = String.valueOf(accountId);
            modalAccountSelector.selectOptionByValue(accountValue);
            modalAccountSelector.shouldHave(Condition.value(accountValue));
            return this;
        });
    }

    public TransferPage enterAmountInModal(double amount) {
        return StepLogger.logWithScreenshot("Enter amount " + amount + " in modal", () -> {
            modalAmountField.shouldBe(Condition.visible)
                    .setValue(String.valueOf(amount));
            return this;
        });
    }

    public TransferPage checkConfirmCheckboxInModal() {
        return StepLogger.logWithScreenshot("Check confirm checkbox in modal", () -> {
            modalConfirmCheckbox.shouldBe(Condition.visible).click();
            return this;
        });
    }

    public TransferPage clickTransferButtonInModal() {
        return StepLogger.logWithScreenshot("Click Send Transfer button in modal", () -> {
            modalTransferButton.shouldBe(Condition.visible, Duration.ofSeconds(10))
                    .shouldBe(Condition.enabled, Duration.ofSeconds(10))
                    .shouldBe(Condition.interactable)
                    .click();
            return this;
        });
    }

    public TransferPage makeRepeatTransferInModal(long fromAccountId, double amount) {
        return StepLogger.log(String.format("Make repeat transfer %s from account %d", amount, fromAccountId),
                () -> selectAccountInModal(fromAccountId)
                        .enterAmountInModal(amount)
                        .checkConfirmCheckboxInModal()
                        .clickTransferButtonInModal());
    }

    /**
     * Получить все операции в истории транзакций
     */
    public List<OperationsList> getAllOperations() {
        return StepLogger.logWithScreenshot("Get all operations from Transaction History", () -> {
            ElementsCollection elementsCollection = $$(".list-group-item");
            return generatePageElements(elementsCollection, OperationsList::new);
        });
    }

    /**
     * Найти первую операцию по типу.
     * Selenide ждет появления элемента (результаты поиска подгружаются асинхронно)
     * и разбирается только найденный элемент, а не весь список
     */
    public OperationsList findFirstOperationByType(String expectedType) {
        return new OperationsList(operationItemOfType(expectedType));
    }

    private SelenideElement operationItemOfType(String expectedType) {
        return $$(".list-group-item")
                .findBy(Condition.text(expectedType + OperationsList.TYPE_AMOUNT_SEPARATOR))
                .shouldBe(Condition.visible);
    }

    /**
     * Кликнуть Repeat на первой операции TRANSFER_OUT.
     * Кнопка Repeat есть только у исходящих переводов: получатель берется из relatedAccountId
     */
    public TransferPage clickRepeatOnFirstTransferOut() {
        return StepLogger.logWithScreenshot("Click Repeat on first TRANSFER_OUT operation", () -> {
            findFirstOperationByType(TRANSFER_OUT).clickRepeat();
            return this;
        });
    }

    /**
     * Проверить, что операция с указанным типом существует
     */
    public TransferPage shouldHaveOperationType(String expectedType) {
        operationItemOfType(expectedType);
        return this;
    }
}
