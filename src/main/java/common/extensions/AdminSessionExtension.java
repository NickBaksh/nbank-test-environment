package common.extensions;

import api.models.dto_model.CreateUserRequest;
import common.annotations.AdminSession;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ui.pages.BasePage;

public class AdminSessionExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        // ШАГ 1: Проверка есть ли у теста аннотация AdminSession
        AdminSession annotation = extensionContext.getRequiredTestMethod().getAnnotation(AdminSession.class);
        if (annotation != null) { // ШАГ 2: Если есть, то добавляем в local storage токен админа
            BasePage.authAsUser(CreateUserRequest.getAdmin());
        }
    }
}
