package iteration_one.ui_tests;

import api.models.CreateUserRequest;
import api.requests.steps.TestUserContext;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import common.annotations.AdminSession;
import common.annotations.Browsers;
import common.annotations.Environments;
import common.annotations.UserSession;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

public class LoginUserTest extends BaseUiTest {

    @Test
    @AdminSession
    @Browsers({"chrome"})
    @Environments("stage")
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = CreateUserRequest.getAdmin();
        Selenide.open("/login");

        new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                .getPage(AdminPanel.class)
                .getAdminPanelText()
                .shouldBe(Condition.visible);
    }

    @Test
    @UserSession
    @Environments
    @Browsers
    public void userCanLoginWithCorrectDataTest() {
        TestUserContext user = getCurrentUser();

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class)
                .getWelcomeTextElement()
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Welcome, noname!"));
    }
}
