package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class OperationsList extends BaseElement{
    private String operationType;
    private double amount;
    private String username;

    public OperationsList(SelenideElement element) {
        super(element);
        String[] lines = element.getText().split("\n");
        String[] firstLineParts = lines[0].split(" - ");

        operationType = firstLineParts[0];
        amount = Double.parseDouble(firstLineParts[1].replace("$", ""));
        username = lines[1].split("Found under: ")[1];
    }

    public void clickRepeat() {
        element.$("button").click();
    }
}
