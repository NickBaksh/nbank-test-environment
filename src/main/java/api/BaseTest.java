package api;

import api.generators.testdata.DataProviders;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import common.extensions.*;
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
@ExtendWith(TimingExtension.class)
public class BaseTest extends DataProviders {

    public static final Long NON_EXISTENT_ACCOUNT_ID = 9999999L;

    private static final ThreadLocal<SoftAssertions> softlyThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<TestContext> testContextThreadLocal = ThreadLocal.withInitial(TestContext::new);

    protected SoftAssertions softly;
    protected TestContext testContext;

    // Конструктор для инициализации testContext
    public BaseTest() {}

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
        softlyThreadLocal.set(softly);

        testContext = testContextThreadLocal.get();

        // Создаем пользователя только для API тестов (если еще не создан)
        // Для UI тестов пользователь создается в BaseUiTest.setUpUser()
        if (getCurrentUser() == null && !isUiTest()) {
            TestUserContext user = UserSteps.createUserWithAccounts("API", "USER", 2);
            setCurrentUser(user);
            System.out.println("✅ API Test user created for thread " +
                    Thread.currentThread().getName() + ": " + user.getDisplayName());
        }
    }

    private boolean isUiTest() {
        // Проверяем, вызывается ли тест из UI пакета
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().contains("iteration_one.ui_tests")) {
                return true;
            }
        }
        return false;
    }

    @AfterEach
    public void afterTest() {
        if (softly != null) {
            softly.assertAll();
            softlyThreadLocal.remove();
        }

        TestContext context = testContextThreadLocal.get();
        if (context != null) {
            // Очищаем пользователей, созданных в текущем тесте
            UserSteps.cleanupTestUser(context.getCurrentUser());
            for (TestUserContext additionalUser : context.getAdditionalUsers()) {
                UserSteps.cleanupTestUser(additionalUser);
            }
            context.clear();
        }
    }

    // ========== Публичные методы для доступа к контексту ==========

    /**
     * Получить текущего пользователя (потокобезопасно)
     */
    public TestUserContext getCurrentUser() {
        TestContext context = testContextThreadLocal.get();
        return context != null ? context.getCurrentUser() : null;
    }

    /**
     * Установить текущего пользователя (потокобезопасно)
     */
    public void setCurrentUser(TestUserContext userContext) {
        TestContext context = testContextThreadLocal.get();
        if (context != null) {
            context.setCurrentUser(userContext);
        }
    }

    /**
     * Получить токен текущего пользователя
     */
    public String getCurrentToken() {
        TestUserContext user = getCurrentUser();
        return user != null ? user.getToken() : null;
    }

    /**
     * Получить первый аккаунт текущего пользователя
     */
    public Long getCurrentFirstAccountId() {
        TestUserContext user = getCurrentUser();
        return user != null ? user.getFirstAccountId() : null;
    }

    /**
     * Получить второй аккаунт текущего пользователя
     */
    public Long getCurrentSecondAccountId() {
        TestUserContext user = getCurrentUser();
        return user != null ? user.getSecondAccountId() : null;
    }

    /**
     * Добавить дополнительного пользователя в контекст
     */
    public void addAdditionalUser(TestUserContext user) {
        TestContext context = testContextThreadLocal.get();
        if (context != null) {
            context.addUser(user);
        }
    }

    /**
     * Получить всех дополнительных пользователей
     */
    public List<TestUserContext> getAdditionalUsers() {
        TestContext context = testContextThreadLocal.get();
        return context != null ? context.getAdditionalUsers() : new ArrayList<>();
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