package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;

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
        return this;
    }

    public EditProfilePage enterName(String name) {
        Selenide.sleep(300);
        nameField.sendKeys(name);
        return this;
    }

    public EditProfilePage saveChanges() {
        saveChangesButton.shouldBe(Condition.visible).click();
        return this;
    }

    public EditProfilePage shouldShowAlert(String expectedMessage) {
        Alert alert = switchTo().alert();
        assertThat(alert.getText()).isEqualTo(expectedMessage);
        alert.accept();
        return this;
    }

    public UserDashboard saveAndReturn() {
        saveChangesButton.click();
        switchTo().alert().accept();
        return new UserDashboard();
    }
}
