package common.extensions;

import api.BaseTest;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

public class UserSessionExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        UserSession annotation = extensionContext.getTestMethod()
                .orElseThrow()
                .getAnnotation(UserSession.class);

        if (annotation != null && annotation.create()) {
            BaseTest testInstance = (BaseTest) extensionContext.getRequiredTestInstance();

            int numberOfUsers = annotation.users();
            int numberOfAccounts = annotation.accounts();
            String role = annotation.role();
            String prefix = annotation.prefix();

            List<TestUserContext> users = new ArrayList<>();

            for (int i = 0; i < numberOfUsers; i++) {
                String userPrefix = numberOfUsers > 1 ? prefix + "_" + (i + 1) : prefix;
                TestUserContext userContext = UserSteps.createUserWithAccounts(
                        userPrefix,
                        role,
                        numberOfAccounts
                );
                users.add(userContext);
                System.out.println("🔐 User " + (i + 1) + " created: " + userContext.getDisplayName());
            }

            // Сохраняем пользователей в тест
            testInstance.setUsers(users);
            if (!users.isEmpty()) {
                testInstance.setCurrentUser(users.get(0));
            }
        }
    }
}