package iteration_one.profile_name_test_cases;

import io.restassured.http.ContentType;
import iteration_one.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ProfileNameChangingOperationsTest extends BaseTest {

    @Test
    @DisplayName("Пользователь может изменить имя профиля на имя из двух слов")
    public void userCanChangeProfileNameToTwoWordsTest() {
        String expectedProfileName = "Catherine Great";

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                           "name": "Catherine Great"
                        }
                        """)
                .put("/api/v1/customer/profile")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("customer.name", equalTo(expectedProfileName))
                .body("message", equalTo("Profile updated successfully"));

        String kateActualProfileName = getKateProfileName();
        Assertions.assertEquals(
                "Catherine Great",
                kateActualProfileName,
                "Profile name updated successfully to " + expectedProfileName);
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
        String kateExpectedProfileName = getKateProfileName();

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                           "name": "%s"
                        }
                        """.formatted(profileName))
                .put("/api/v1/customer/profile")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo("Name must contain two words with letters only"));

        String kateActualProfileName = getKateProfileName();
        Assertions.assertEquals(
                kateExpectedProfileName,
                kateActualProfileName,
                "Kate profile name does not change");
    }

    @Test
    @DisplayName("Пользователь не может использовать кириллицу в имени")
    public void userCannotUseCyrillicLettersInNameTest() {
        String kateExpectedProfileName = getKateProfileName();

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                           "name": "Екатерина Великая"
                        }
                        """)
                .put("/api/v1/customer/profile")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo("Name must contain two words with letters only"));

        String kateActualProfileName = getKateProfileName();
        Assertions.assertEquals(
                kateExpectedProfileName,
                kateActualProfileName,
                "Kate profile name does not change");
    }
}
