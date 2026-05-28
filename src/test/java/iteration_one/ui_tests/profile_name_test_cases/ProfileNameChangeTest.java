package iteration_one.ui_tests.profile_name_test_cases;

import api.generators.TestUser;
import iteration_one.ui_tests.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ui.pages.UserDashboard;

import static ui.pages.BankAlert.NAME_MUST_CONTAIN_TWO_WORDS;
import static ui.pages.BankAlert.NAME_UPDATED_SUCCESSFULLY;

public class ProfileNameChangeTest extends BaseUiTest {

    @Test
    @DisplayName("Проверка изменения имени профиля на валидное значение (два слова состоящие только из букв)")
    public void userCanChangeProfileNameTest() {
        // Данные для теста
        String expectedUsername = getCustomerUsername(TestUser.KATE.getKey());
        String validName = validProfileName();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authAsUser(TestUser.KATE);

        // ШАГ 2: Перейти на страницу "User Dashboard"
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
        String profileNameAfterTest = getCustomerProfileName(TestUser.KATE.getKey());
        softly.assertThat(profileNameAfterTest).isEqualTo(validName);
    }


    @Test
    @DisplayName("Проверка UI валидации при отправке некорректного имени профиля")
    public void userCannotSaveEmptyProfileNameTest() {

        // Данные для теста
        String expectedUsername = getCustomerUsername(TestUser.KATE.getKey());
        String profileNameExpected = getCustomerProfileName(TestUser.KATE.getKey());
        String invalidName = invalidProfileName();

        // ШАГ 1: Авторизоваться под учетной записью пользователя
        authAsUser(TestUser.KATE);

        // ШАГ 2: Перейти на страницу "User Dashboard"
        new UserDashboard()
                .open()
                .shouldHaveWelcomeText(WELCOME_TEXT_DEFAULT)
                .shouldHaveUsername(expectedUsername)
                .goToEditProfile()
                .shouldHaveEditProfileText(EDIT_PROFILE_TEXT)
                .clearName()
                .enterName(invalidName)
                .saveChanges()
                .shouldShowAlert(NAME_MUST_CONTAIN_TWO_WORDS.getMessage());

        // ШАГ 3: Проверить, что имя профиля обновилось на сервере
        String profileNameActual = getCustomerProfileName(TestUser.KATE.getKey());
        softly.assertThat(profileNameActual).isEqualTo(profileNameExpected);
    }
}
