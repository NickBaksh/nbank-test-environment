package iteration_one.transactions_test_cases;

import io.restassured.http.ContentType;
import iteration_one.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class TransactionOperationsTest extends BaseTest {
    @BeforeEach
    public void setUpBalance() {

        int i = 0;
        //Пополняем счёт Кейт на 20000 перед каждым тестом перевода
        //Вызов несколько раз, т.к. есть ограничение на пополнение в 5000
        while (i < 4) {
            i += 1;
            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .header("Authorization", KATE_AUTH_TOKEN)
                    .body("""
                         {
                          "id": %s,
                          "balance": 5000
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST))
                    .post("/api/v1/accounts/deposit")
                    .then()
                    .statusCode(HttpStatus.SC_OK);
                    //не добавил проверку на появлении депозита на аккаунте,
                    //показалось довольно сложным для этого уровня
        }
    }

    @ParameterizedTest
    @CsvSource({
            "0.01, 200",
            "9999.99, 200",
            "10000, 200"
    })
    @DisplayName("Проверка отправки валидной суммы на аккаунт другого человека")
    public void userCanTransferMoneyToAnotherAccountTest(float transferSum) {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": %s
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST, ALEX_ACCOUNT_ID_FIRST, transferSum))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", containsString("Transfer successful"))
                .body("amount", is(equalTo(transferSum)));
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, 400",
            "0, 400"
    })
    @DisplayName("Проверка невозможности отправки невалидной суммы на аккаунт другого человека")
    public void userCannotTransferInvalidSumToAnotherAccountTest(float transferSum, int expectedResponse) {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": %s
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST, ALEX_ACCOUNT_ID_FIRST, transferSum))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(expectedResponse)
                .body(equalTo("Transfer amount must be at least 0.01"));

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": 10000.01
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST, ALEX_ACCOUNT_ID_FIRST))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo("Transfer amount cannot exceed 10000"));
    }

    @Test
    @DisplayName("Проверка невозможности отправки суммы больше чем есть на счёте")
    public void userCannotTransferMoreMoneyThanHaveTest() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": 10000
                        }
                        """.formatted(KATE_ACCOUNT_ID_SECOND, ALEX_ACCOUNT_ID_FIRST))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }

    @Test
    @DisplayName("Проверка перевода между своими счетами")
    public void userCanTransferMoneyBetweenAccountsTest() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": 1
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST, KATE_ACCOUNT_ID_SECOND))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", containsString("Transfer successful"))
                .body("amount", is(1.0f));
    }

    @Test
    @DisplayName("Проверка невозможности перевода на несуществующий счёт")
    public void userCannotTransferMoneyToNonExistentAccountTest() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": 9999,
                          "amount": 1
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }

    @Test
    @DisplayName("Проверка невозможности перевода без авторизации")
    public void userCannotTransferMoneyWithoutAuthorizationTest() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": 1
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST, ALEX_ACCOUNT_ID_FIRST))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("Проверка перевода от одного клиента другому. Возврат от Алекса к Кейт")
    public void userCanTransferMoneyToSenderTest() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ALEX_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": 1111
                        }
                        """.formatted(ALEX_ACCOUNT_ID_FIRST, KATE_ACCOUNT_ID_SECOND))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", containsString("Transfer successful"))
                .body("amount", is(1111.0f));
    }

    @Test
    @DisplayName("Проверка невозможности перевода с авторизацией на своем аккаунте, но трансфере с чужого аккаунта")
    public void userCannotTransferMoneyFromAnotherUsersAccountTest() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": 1
                        }
                        """.formatted(ALEX_ACCOUNT_ID_FIRST, KATE_ACCOUNT_ID_SECOND))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(equalTo("Unauthorized access to account"));
    }

    @Test
    @DisplayName("Проверка невозможности перевода со своего аккаунта на тот же аккаунт")
    public void userCannotTransferMoneyToSameAccountTest() {
        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": 1
                        }
                        """.formatted(KATE_ACCOUNT_ID_SECOND, KATE_ACCOUNT_ID_SECOND))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo("Invalid transfer: insufficient funds or invalid accounts"));
    }
}
