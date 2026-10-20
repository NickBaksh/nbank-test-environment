package iteration_one.ui_tests.profile_name_test_cases;

import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import common.annotations.Browsers;
import common.annotations.Environments;
import common.annotations.UserSession;
import iteration_one.ui_tests.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.selenide.videorecorder.core.Video;
import ui.pages.UserDashboard;

import static ui.pages.BankAlert.NAME_MUST_CONTAIN_TWO_WORDS;
import static ui.pages.BankAlert.NAME_UPDATED_SUCCESSFULLY;
import static ui.pages.BasePage.authWithToken;

public class ProfileNameChangeTest extends BaseUiTest {

    @Test
    @DisplayName("Проверка изменения имени профиля на валидное значение (два слова состоящие только из букв)")
    @UserSession
    @Environments
    @Browsers
    public void userCanChangeProfileNameTest() {
        // Данные для теста
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        String expectedUsername = user.getUsername();
        String validName = UserSteps.generateValidProfileName();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToEditProfile()
                .shouldHaveEditProfileText(EDIT_PROFILE_TEXT)
                .clearName()
                .enterName(validName)
                .saveChanges()
                .shouldShowAlert(NAME_UPDATED_SUCCESSFULLY.getMessage());

        // ШАГ 3: Проверить, что имя профиля обновилось на сервере
        String profileNameAfterTest = UserSteps.getProfileName(user);
        softly.assertThat(profileNameAfterTest).isEqualTo(validName);
    }


    @ParameterizedTest
    @MethodSource("invalidProfileNamesUi")
    @DisplayName("Проверка UI валидации при отправке некорректного имени профиля")
    @Environments
    @Browsers
    @UserSession
    public void userCannotSaveInvalidProfileNameTest(String invalidName, String expectedAlert) {

        // Данные для теста
        TestUserContext user = getCurrentUser();

        String token = user.getToken();
        String expectedUsername = user.getUsername();
        String profileNameExpected = UserSteps.getProfileName(user);

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authWithToken(token);

        // ШАГ 2: Выполнить шаги теста
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToEditProfile()
                .shouldHaveEditProfileText(EDIT_PROFILE_TEXT)
                .clearName()
                .enterName(invalidName)
                .saveChanges()
                .saveChangesAndVerifyAlert(expectedAlert);

        // ШАГ 3: Проверить, что имя профиля обновилось на сервере
        String profileNameActual = UserSteps.getProfileName(user);
        softly.assertThat(profileNameActual).isEqualTo(profileNameExpected);
    }
}
