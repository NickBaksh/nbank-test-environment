package ui.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import common.helpers.StepLogger;
import lombok.Getter;
import ui.elements.UserBadge;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    private SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));

    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password) {
        return StepLogger.logWithScreenshot("Create user '" + username + "' via Admin Panel", () -> {
            usernameInput.sendKeys(username);
            passwordInput.sendKeys(password);
            button.click();
            return this;
        });
    }

    public List<UserBadge> getAllUsers() {
        return StepLogger.logWithScreenshot("Get all users from Admin Panel", () -> {
            ElementsCollection elementsCollection = $(Selectors.byText("All Users")).parent().findAll("li");
            return generatePageElements(elementsCollection, UserBadge::new);
        });
    }
}
