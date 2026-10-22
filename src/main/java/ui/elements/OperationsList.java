package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class OperationsList extends BaseElement {
    // Разделитель между типом и суммой операции (em dash)
    public static final String TYPE_AMOUNT_SEPARATOR = " — $";

    // Текст элемента: "TRANSFER_OUT — $100.00\n<дата> • Status: ... • Related Account ID: 2\n🔁 Repeat"
    private static final Pattern OPERATION_PATTERN =
            Pattern.compile("^(\\w+) — \\$([\\d.]+)");
    private static final Pattern RELATED_ACCOUNT_PATTERN =
            Pattern.compile("Related Account ID:[ \\t]*(\\d+)");

    private String operationType;
    private double amount;
    private Long relatedAccountId;

    public OperationsList(SelenideElement element) {
        super(element);
        String text = element.getText();
        Matcher matcher = OPERATION_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalStateException("Unexpected operation item text: '" + text + "'");
        }

        operationType = matcher.group(1);
        amount = Double.parseDouble(matcher.group(2));

        Matcher relatedAccount = RELATED_ACCOUNT_PATTERN.matcher(text);
        relatedAccountId = relatedAccount.find() ? Long.parseLong(relatedAccount.group(1)) : null;
    }

    public void clickRepeat() {
        element.$("button").click();
    }
}
