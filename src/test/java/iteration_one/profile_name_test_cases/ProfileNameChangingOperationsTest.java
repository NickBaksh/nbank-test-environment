package iteration_one.profile_name_test_cases;

import iteration_one.BaseTest;
import models.Customer;
import models.UpdateCustomerProfileRequest;
import models.UpdateCustomerProfileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import requests.requesters.put.UpdateCustomerProfileRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.hamcrest.Matchers.equalTo;
import static specs.ResponseSpecs.PROFILE_NAME_FORMAT_ERROR;
import static specs.ResponseSpecs.PROFILE_UPDATE_SUCCESS;

public class ProfileNameChangingOperationsTest extends BaseTest {

    @ParameterizedTest
    @CsvSource({
            "Catherine Great"
    })
    @DisplayName("Пользователь может изменить имя профиля на имя из двух слов")
    public void userCanChangeProfileNameToTwoWordsTest(String profileName) {
        UpdateCustomerProfileRequest request = UpdateCustomerProfileRequest
                .builder()
                .name(profileName)
                .build();

        UpdateCustomerProfileResponse response = new UpdateCustomerProfileRequester(
                RequestSpecs.authWithTokenSpec(token(USER_KATE)),
                ResponseSpecs.requestReturnsOK())
                .put(request)
                .extract()
                .as(UpdateCustomerProfileResponse.class);

        // Использую сериализацию для сравнения нового имени профиля с тем значением, которое передал в запросе
        // Сравниваю message с ожидаемым значением
        softly.assertThat(response.getMessage()).isEqualTo(PROFILE_UPDATE_SUCCESS);
        softly.assertThat(response.getCustomer().getName()).isEqualTo(profileName);
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
                .body(equalTo(PROFILE_NAME_FORMAT_ERROR));

        //Для негативных проверок запрашиваю через ГЕТ состояние профиля клиента после теста.
        // Ответ с ошибкой 400 нет смысла сериализовать, т.к. при 400 ответе возвращается только
        // текст ошибки не в json
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
                .body(equalTo(PROFILE_NAME_FORMAT_ERROR));

        String profileNameAfter = getCustomerProfileName(USER_KATE);

        softly.assertThat(profileNameAfter)
                .as("Profile name should not be updated")
                .isEqualTo(profileNameBefore);
    }
}
