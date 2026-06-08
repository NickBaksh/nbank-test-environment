package api;

import api.generators.testdata.DataProviders;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import common.extensions.AdminSessionExtension;
import common.extensions.BrowserMatchExtension;
import common.extensions.EnvironmentMatchExtension;
import common.extensions.UserSessionExtension;
import lombok.Setter;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(BrowserMatchExtension.class)
@ExtendWith(EnvironmentMatchExtension.class)
public class BaseTest extends DataProviders {

    public static final Long NON_EXISTENT_ACCOUNT_ID = 9999999L;

    protected SoftAssertions softly;
    protected TestContext testContext;

    // Конструктор для инициализации testContext
    public BaseTest() {
        this.testContext = new TestContext();
    }

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
//        this.testContext.clear();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
        // Очищаем пользователей, созданных в тесте
        UserSteps.cleanupTestUsers();
    }

    // ========== Публичные методы для доступа к контексту ==========

    /**
     * Получить текущего пользователя
     */
    public TestUserContext getCurrentUser() {
        return this.testContext.getCurrentUser();
    }

    /**
     * Установить текущего пользователя
     */
    public void setCurrentUser(TestUserContext userContext) {
        this.testContext.setCurrentUser(userContext);
    }

    /**
     * Получить токен текущего пользователя
     */
    public String getCurrentToken() {
        return this.testContext.getCurrentUser().getToken();
    }

    /**
     * Получить первый аккаунт текущего пользователя
     */
    public Long getCurrentFirstAccountId() {
        return this.testContext.getCurrentUser().getFirstAccountId();
    }

    /**
     * Получить второй аккаунт текущего пользователя
     */
    public Long getCurrentSecondAccountId() {
        return this.testContext.getCurrentUser().getSecondAccountId();
    }

    // Вспомогательный класс для хранения контекста теста
    protected static class TestContext {
        private final List<TestUserContext> additionalUsers = new ArrayList<>();
        @Setter
        private TestUserContext currentUser;

        public TestUserContext getCurrentUser() {
            return currentUser;
        }

        public void addUser(TestUserContext user) {
            additionalUsers.add(user);
        }

        public List<TestUserContext> getAdditionalUsers() {
            return additionalUsers;
        }

        public TestUserContext getFirstAdditionalUser() {
            return additionalUsers.isEmpty() ? null : additionalUsers.get(0);
        }

        public void clear() {
            currentUser = null;
            additionalUsers.clear();
        }
    }
}