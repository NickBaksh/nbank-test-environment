package iteration_one.api_tests.profile_name_test_cases;

import api.generators.RandomModelGenerator;
import api.generators.TestUser;
import api.generators.testdata.InvalidNameCase;
import iteration_one.api_tests.BaseTest;
import api.models.UpdateCustomerProfileRequest;
import api.models.UpdateCustomerProfileResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static api.specs.ResponseSpecs.PROFILE_NAME_FORMAT_ERROR;
import static api.specs.ResponseSpecs.PROFILE_UPDATE_SUCCESS;

public class ProfileNameChangingOperationsTest extends BaseTest {

    @ParameterizedTest
    // Данные для теста беру из метода в BaseTest
    @MethodSource("validProfileNames")
    @DisplayName("Пользователь может изменить имя профиля на имя из двух слов")
    public void userCanChangeProfileNameToTwoWordsTest(String profileName) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        UpdateCustomerProfileResponse response = new ValidatedCrudRequester<UpdateCustomerProfileResponse>(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE_UPDATE)
                .update(request);

        ModelAssertions.assertThatModels(request, response).match();

        // Сравниваю message с ожидаемым значением
        softly.assertThat(response.getMessage()).isEqualTo(PROFILE_UPDATE_SUCCESS);
    }

    @ParameterizedTest
    // Данные для теста беру из метода в BaseTest
    @MethodSource("invalidProfileNames")
    @DisplayName("Пользователь не может изменить имя профиля, " +
            "если оно не соответствует формату 'Слово пробел Слово'")
    public void userCannotChangeProfileNameNotMatchingTwoWordsFormatTest(String profileName) {
        String profileNameBefore = getCustomerProfileName(TestUser.KATE.getKey());

        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        new CrudRequester(
                RequestSpecs.authWithTokenSpec(token(TestUser.KATE.getKey())),
                ResponseSpecs.returnsBadRequest(),
                Endpoint.CUSTOMER_PROFILE_UPDATE)
                .update(request)
                .body(equalTo(PROFILE_NAME_FORMAT_ERROR));

        String profileNameAfter = getCustomerProfileName(TestUser.KATE.getKey());
        softly.assertThat(profileNameAfter)
                .as("Profile name should not be updated")
                .isEqualTo(profileNameBefore);
    }
}
