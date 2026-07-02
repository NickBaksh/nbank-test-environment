package iteration_one.ui_tests;

import api.BaseTest;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
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

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true));

        // Увеличенные таймауты для UI тестов
        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 60000;
    }

    @BeforeEach
    public void setUp() {
        // чистим куки и storage
        Selenide.open("/");

        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
        Selenide.localStorage().clear();
        Selenide.sessionStorage().clear();
    }

    @BeforeEach
    public void setUpUser() {
        // Создаем нового пользователя перед каждым тестом
        currentUser = UserSteps.createUserWithAccounts("User", "USER", 2);

        // Пополняем баланс (нужно для тестов перевода)
        UserSteps.setUpBalance(currentUser);

        // Открываем базовую страницу
        open("/");

        System.out.println("✅ Test user created: " + currentUser.getDisplayName());
    }

    @AfterEach
    public void tearDownUser() {
        // Удаляем пользователя после теста
        if (currentUser != null) {
            UserSteps.cleanupTestUsers();
            System.out.println("✅ Test user deleted: " + currentUser.getUsername());
        }
    }
}
