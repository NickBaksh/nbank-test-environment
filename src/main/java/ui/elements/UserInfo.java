package ui.elements;

import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

@Getter
public class UserInfo extends BaseElement {
    private final SelenideElement profileNameElement;
    private final SelenideElement usernameElement;

    public UserInfo(SelenideElement element) {
        super(element);
        profileNameElement = element.$(".user-name");
        usernameElement = element.$(".user-username");
    }

    public String getProfileName() {
        return profileNameElement.getText().trim();
    }

    public String getUsername() {
        // Убираем @ и пробелы
        return usernameElement.getText()
                .replace("@", "")
                .trim();
    }

    public String getFullUsername() {
        // Возвращает username с @
        return usernameElement.getText().trim();
    }

    public boolean hasProfileName() {
        return profileNameElement.exists() && !profileNameElement.getText().isEmpty();
    }
}
