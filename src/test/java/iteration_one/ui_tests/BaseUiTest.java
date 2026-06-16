package iteration_one.ui_tests;

import api.BaseTest;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Map;

import static com.codeborne.selenide.Selenide.open;

public class BaseUiTest extends BaseTest {

    private TestUserContext currentUser;

    protected static final String WELCOME_TEXT_DEFAULT = "Welcome, noname!";
    protected static final String DEPOSIT_MONEY_TEXT = "💰 Deposit Money";
    protected static final String EDIT_PROFILE_TEXT = "✏️ Edit Profile";
    protected static final String SAVE_CHANGES_TEXT = "💾 Save Changes";

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = api.configs.Config.getProperty("uiRemote");
        Configuration.baseUrl = api.configs.Config.getProperty("uiBaseUrl");
        Configuration.browser = api.configs.Config.getProperty("Configuration.browser");
        Configuration.browserSize = api.configs.Config.getProperty("Configuration.browserSize");
        Configuration.headless = true;

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));

        // Увеличенные таймауты для UI тестов
        Configuration.timeout = 15000;
        Configuration.pageLoadTimeout = 60000;

        Configuration.holdBrowserOpen = false;
    }

    @BeforeEach
    public void setUp() {
        if (WebDriverRunner.hasWebDriverStarted()) {
            Selenide.closeWebDriver();
        }

        open("/");

        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
        Selenide.executeJavaScript("window.localStorage.clear();");
        Selenide.executeJavaScript("window.sessionStorage.clear();");
    }

    @AfterEach
    public void tearDownUser() {
        // Удаляем пользователя после теста
        if (currentUser != null) {
            UserSteps.cleanupTestUsers();
            System.out.println("✅ Test user deleted: " + currentUser.getUsername());
        }

        if (WebDriverRunner.hasWebDriverStarted()) {
            Selenide.closeWebDriver();
            try {
                WebDriverRunner.getWebDriver().quit();
            } catch (Exception e) {
                System.out.println("WebDriver already closed");
            }
        }
    }
}