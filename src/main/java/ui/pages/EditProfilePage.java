package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import utils.AlertHelper;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public class EditProfilePage extends BasePage<EditProfilePage> {
    public static SelenideElement editProfilePageText = $(Selectors.byText("✏\uFE0F Edit Profile"));
    private SelenideElement nameField = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton = $(Selectors.byText("\uD83D\uDCBE Save Changes"));


    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage shouldHaveEditProfileText (String expectedText) {
        editProfilePageText.shouldBe(Condition.visible).shouldHave(Condition.text(expectedText));
        return this;
    }

    public EditProfilePage clearName() {
        nameField.clear();
        nameField.shouldHave(Condition.empty);
        Selenide.screenshot("clearName_changeProfileName");
        return this;
    }

    public EditProfilePage enterName(String name) {
        assertThat(name).isNotNull();

        nameField.shouldBe(Condition.visible);
        nameField.clear();

        // Проверяем, что поле стало пустым
        nameField.shouldHave(Condition.empty);

        // Если имя не пустое и не состоит только из пробелов
        if (name != null && !name.trim().isEmpty()) {
            nameField.sendKeys(name);
            nameField.shouldHave(Condition.value(name));
        }
        return this;
    }

    public EditProfilePage saveChanges() {
        saveChangesButton
                .shouldBe(Condition.visible)
                .shouldBe(Condition.enabled)
                .shouldBe(Condition.interactable)
                .click();
        return this;
    }

    public EditProfilePage shouldShowAlert(String expectedMessage) {
//        Selenide.sleep(500);
        Selenide.confirm(expectedMessage);
        return this;
    }

    public UserDashboard saveAndReturn() {
        saveChangesButton.click();
        switchTo().alert().accept();
        return new UserDashboard();
    }

    public EditProfilePage saveChangesAndVerifyAlert(String expectedMessage) {
        // Кликаем и ждем alert с автоматическими повторными попытками
        AlertHelper.clickAndVerifyAlert(saveChangesButton, expectedMessage);
        return this;
    }
}
