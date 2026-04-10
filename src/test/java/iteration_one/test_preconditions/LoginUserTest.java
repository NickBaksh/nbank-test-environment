package iteration_one.test_preconditions;

import io.restassured.http.ContentType;
import iteration_one.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest extends BaseTest {

    @Test
    @DisplayName("Проверка, что в системе были созданы аккаунты для Кейт и Алекса, " +
            "а также их счета. Два счёта у Кейт и один у Алекса")
    public void userProfilesAndAccountsWasCreatedTest() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("Authorization", ADMIN_AUTH_TOKEN)
                .accept(ContentType.JSON)
                .get("/api/v1/admin/users")
                .then()
                .log().body()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.username.startsWith('Kate_') && it.accounts.size() == 2 }", notNullValue())
                .body("find { it.username.startsWith('Alex_') && it.accounts.size() == 1 }", notNullValue());
    }
}
