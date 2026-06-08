package iteration_one.ui_tests;

import api.BaseTest;
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.BeforeAll;

import java.util.Map;

public class BaseUiTest extends BaseTest {

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

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));

        // Увеличенные таймауты для UI тестов
        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 60000;
    }
}