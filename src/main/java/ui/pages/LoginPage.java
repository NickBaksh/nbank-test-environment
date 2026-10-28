package ui.pages;

import common.helpers.StepLogger;

public class LoginPage extends BasePage<LoginPage> {
    @Override
    public String url() {
        return "/login";
    }

    public LoginPage login(String username, String password) {
        return StepLogger.logWithScreenshot("Login as '" + username + "'", () -> {
            usernameInput.sendKeys(username);
            passwordInput.sendKeys(password);
            button.click();
            return this;
        });
    }
}
