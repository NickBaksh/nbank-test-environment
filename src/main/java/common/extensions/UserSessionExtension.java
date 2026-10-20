package common.extensions;

import api.BaseTest;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class UserSessionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getTestMethod()
                .orElseThrow()
                .getAnnotation(UserSession.class);

        if (annotation != null && annotation.create()) {
            // Получаем экземпляр теста
            BaseTest testInstance = (BaseTest) extensionContext.getRequiredTestInstance();

            // Создаём пользователя с аккаунтами через API
            TestUserContext userContext = UserSteps.createUserWithAccounts(
                    annotation.prefix(),
                    annotation.role(),
                    annotation.accounts()
            );

            // Сохраняем пользователя в контекст теста
            testInstance.setCurrentUser(userContext);

            System.out.println("🔐 User created via API: " + userContext.getDisplayName());
        }
    }
}