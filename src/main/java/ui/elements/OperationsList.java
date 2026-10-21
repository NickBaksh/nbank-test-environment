package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class OperationsList extends BaseElement {
    // Текст элемента: "TRANSFER_IN - $100.00\n🔍 Found under: Имя\n🔁 Repeat"
    private static final Pattern OPERATION_PATTERN =
            Pattern.compile("^(\\w+) - \\$([\\d.]+)\\R.*?Found under:[ \\t]*([^\\r\\n]*)", Pattern.DOTALL);

    private String operationType;
    private double amount;
    private String username;

    public OperationsList(SelenideElement element) {
        super(element);
        String text = element.getText();
        Matcher matcher = OPERATION_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalStateException("Unexpected operation item text: '" + text + "'");
        }

        operationType = matcher.group(1);
        amount = Double.parseDouble(matcher.group(2));
        username = matcher.group(3).trim();
    }

    public void clickRepeat() {
        element.$("button").click();
    }
}
