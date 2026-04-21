package iteration_one;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

public class BaseTest {

    public static String ADMIN_AUTH_TOKEN;
    public static String KATE_AUTH_TOKEN;
    public static String ALEX_AUTH_TOKEN;

    public static int KATE_ACCOUNT_ID_FIRST;
    public static int KATE_ACCOUNT_ID_SECOND;
    public static int ALEX_ACCOUNT_ID_FIRST;

    @BeforeAll
    public static void init() {
        baseURI = "http://localhost:4111";

        // Авторизация как администратор и сохранение токена
        ADMIN_AUTH_TOKEN = getToken("admin", "admin");

        // Создаем <случайные> имена Кейт и Алекса
        String alex = "Alex_" + UUID.randomUUID().toString().substring(0, 8);
        String kate = "Kate_" + UUID.randomUUID().toString().substring(0, 8);

        // Создание пользователей (Кейт и Алекс)
        createUser(kate, ADMIN_AUTH_TOKEN);
        createUser(alex, ADMIN_AUTH_TOKEN);

        // Авторизация под пользователями и сохранение их токена авторизации
        KATE_AUTH_TOKEN = getToken(kate, "verysTRongPassword33$");
        ALEX_AUTH_TOKEN = getToken(alex, "verysTRongPassword33$");

        KATE_ACCOUNT_ID_FIRST = createAccount(KATE_AUTH_TOKEN);
        KATE_ACCOUNT_ID_SECOND = createAccount(KATE_AUTH_TOKEN);
        ALEX_ACCOUNT_ID_FIRST = createAccount(ALEX_AUTH_TOKEN);

    }

    // Метод для сохранения токена авторизованного пользователя
    public static String getToken(String username, String password) {
        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                            "username": "%s",
                            "password": "%s"
                        }
                        """.formatted(username, password))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().header("Authorization");
    }

    // Метод для создания пользователя в системе
    public static void createUser(String username, String adminToken) {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", adminToken)
                .body("""
                        {
                           "username": "%s",
                           "password": "verysTRongPassword33$",
                           "role": "USER"
                         }
                        """.formatted(username))
                .post("/api/v1/admin/users")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED);
    }

    public static int createAccount(String token) {
        return given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/api/v1/accounts")
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .jsonPath()
                .getInt("id");
    }

    public static double getBalanceByAccountId(String authToken, int accountId) {
        Number balance = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", authToken)
                .get("/api/v1/customer/accounts")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().path("find { it.id == " + accountId + " }.balance");
        return balance.doubleValue();
    }

    public static double getCurrentBalanceKateFirstAccount() {
        return getBalanceByAccountId(KATE_AUTH_TOKEN, KATE_ACCOUNT_ID_FIRST);
    }

    public static double getCurrentBalanceKateSecondAccount() {
        return getBalanceByAccountId(KATE_AUTH_TOKEN, KATE_ACCOUNT_ID_SECOND);
    }

    public static double getCurrentBalanceAlexFirstAccount() {
        return getBalanceByAccountId(ALEX_AUTH_TOKEN, ALEX_ACCOUNT_ID_FIRST);
    }

    public static String getKateProfileName() {
            return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .get("/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().path("name");
    }
}
