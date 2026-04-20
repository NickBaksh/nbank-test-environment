package iteration_one.profile_name_test_cases;

import iteration_one.BaseTest;
import models.UpdateCustomerProfileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import requests.requesters.put.UpdateCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.hamcrest.Matchers.equalTo;

public class ProfileNameChangingOperationsTest extends BaseTest {

    @ParameterizedTest
    @CsvSource({
            "Catherine Great"
    })
    @DisplayName("Пользователь может изменить имя профиля на имя из двух слов")
    public void userCanChangeProfileNameToTwoWordsTest(String profileName) {
        String profileNameBefore = getCustomerProfileName(USER_KATE);

        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        new UpdateCustomerProfileRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.requestReturnsOK())
                .put(request)
                .body("message", equalTo("Profile updated successfully"));

        String profileNameAfter = getCustomerProfileName(USER_KATE);

        softly.assertThat(profileNameAfter)
                .as("Profile name should be updated to " + profileName)
                .isEqualTo(profileName);

        softly.assertThat(profileNameAfter)
                .as("Profile name should change")
                .isNotEqualTo(profileNameBefore);
    }

    @ParameterizedTest
    @CsvSource({
            "Catherine The Great",
            "Catherine",
            "' '",
            "''",
            "Catherine12 Great",
            "Catherine 12Great",
            "Cath&rine Great",
            "Catherine Gre@t",
            "Catherine_Great"
    })
    @DisplayName("Пользователь не может изменить имя профиля, " +
            "если оно не соответствует формату 'Слово пробел Слово'")
    public void userCannotChangeProfileNameNotMatchingTwoWordsFormatTest(String profileName) {
        String profileNameBefore = getCustomerProfileName(USER_KATE);

        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        new UpdateCustomerProfileRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .put(request)
                .body(equalTo("Name must contain two words with letters only"));

        String profileNameAfter = getCustomerProfileName(USER_KATE);

        softly.assertThat(profileNameAfter)
                .as("Profile name should not be updated")
                .isEqualTo(profileNameBefore);
    }

    @ParameterizedTest
    @CsvSource({
            "Екатерина Великая"
    })
    @DisplayName("Пользователь не может использовать кириллицу в имени")
    public void userCannotUseCyrillicLettersInNameTest(String profileName) {
        String profileNameBefore = getCustomerProfileName(USER_KATE);

        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        new UpdateCustomerProfileRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.returnsBadRequest())
                .put(request)
                .body(equalTo("Name must contain two words with letters only"));

        String profileNameAfter = getCustomerProfileName(USER_KATE);

        softly.assertThat(profileNameAfter)
                .as("Profile name should not be updated")
                .isEqualTo(profileNameBefore);
    }
}
