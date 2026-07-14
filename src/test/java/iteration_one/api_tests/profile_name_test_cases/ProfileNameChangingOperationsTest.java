package iteration_one.api_tests.profile_name_test_cases;

import api.BaseTest;
import api.models.dto_model.UpdateCustomerProfileRequest;
import api.models.dto_model.UpdateCustomerProfileResponse;
import api.models.comparison.ModelAssertions;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.DataBaseSteps;
import api.requests.steps.TestUserContext;
import api.requests.steps.UserSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import common.annotations.UserSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static api.specs.ResponseSpecs.PROFILE_NAME_FORMAT_ERROR;
import static api.specs.ResponseSpecs.PROFILE_UPDATE_SUCCESS;
import static org.hamcrest.Matchers.equalTo;

public class ProfileNameChangingOperationsTest extends BaseTest {

    @ParameterizedTest
    // Данные для теста беру из метода в BaseTest
    @MethodSource("validProfileNames")
    @DisplayName("Пользователь может изменить имя профиля на имя из двух слов")
    @UserSession
    public void userCanChangeProfileNameToTwoWordsTest(String profileName) {

        TestUserContext user = getCurrentUser();
        String token = user.getToken();

        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        UpdateCustomerProfileResponse response = new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE_UPDATE)
                .update(request);

        ModelAssertions.assertThatModels(request, response).match();

        // Сравниваю message с ожидаемым значением
        softly.assertThat(response.getMessage()).isEqualTo(PROFILE_UPDATE_SUCCESS);

        DataBaseSteps.verifyCustomerProfileName(softly, user.getUsername(), profileName);
    }

    @ParameterizedTest
    @MethodSource("invalidProfileNames")
    @DisplayName("Пользователь не может изменить имя профиля, " +
            "если оно не соответствует формату 'Слово пробел Слово'")
    @UserSession
    public void userCannotChangeProfileNameNotMatchingTwoWordsFormatTest(String profileName) {

        TestUserContext user = getCurrentUser();
        String profileNameBefore = UserSteps.getProfileName(user);
        String token = user.getToken();

        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.CUSTOMER_PROFILE_UPDATE)
                .update(request)
                .body(equalTo(PROFILE_NAME_FORMAT_ERROR));

        String profileNameAfter = UserSteps.getProfileName(user);

        softly.assertThat(profileNameAfter)
                .as("Profile name should not be updated")
                .isEqualTo(profileNameBefore);

        DataBaseSteps.verifyCustomerProfileNameUnchanged(softly, user.getUsername(), profileNameBefore);
    }
}
