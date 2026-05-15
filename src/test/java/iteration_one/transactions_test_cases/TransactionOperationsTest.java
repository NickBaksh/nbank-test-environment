package iteration_one.transactions_test_cases;

import io.restassured.http.ContentType;
import iteration_one.BaseTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

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
        }

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ALEX_AUTH_TOKEN)
                .body("""
                         {
                          "id": %s,
                          "balance": 5000
                        }
                        """.formatted(ALEX_ACCOUNT_ID_FIRST))
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @ParameterizedTest
    @CsvSource({
            "0.01",
            "9999.99",
            "10000"
    })
    @DisplayName("Проверка отправки валидной суммы на аккаунт другого человека")
    public void userCanTransferMoneyToAnotherAccountTest(double transferSum) {
        double kateBalanceBefore = getCurrentBalanceKateFirstAccount();
        double kateExpectedBalance = kateBalanceBefore - transferSum;

        double alexBalanceBefore = getCurrentBalanceAlexFirstAccount();
        double alexExpectedBalance = alexBalanceBefore + transferSum;

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
                .body("amount", equalTo((float) transferSum));

        double kateActualBalance = getCurrentBalanceKateFirstAccount();
        double alexActualBalance = getCurrentBalanceAlexFirstAccount();

        Assertions.assertEquals(
                kateExpectedBalance,
                kateActualBalance,
                0.01,
                "Kate's balance should be decreased by " + transferSum);

        Assertions.assertEquals(
                alexExpectedBalance,
                alexActualBalance,
                0.01,
                "Alex's balance should be increased by " + transferSum);
    }

    @ParameterizedTest
    @CsvSource({
            "-0.01, Transfer amount must be at least 0.01",
            "0, Transfer amount must be at least 0.01",
            "10000.01, Transfer amount cannot exceed 10000"
    })
    @DisplayName("Проверка невозможности отправки невалидной суммы на аккаунт другого человека")
    public void userCannotTransferInvalidSumToAnotherAccountTest(double transferSum, String expectedResponse) {
        double kateExpectedBalance = getCurrentBalanceKateFirstAccount();
        double alexExpectedBalance = getCurrentBalanceAlexFirstAccount();

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
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo(expectedResponse));

        double kateActualBalance = getCurrentBalanceKateFirstAccount();
        double alexActualBalance = getCurrentBalanceAlexFirstAccount();

        Assertions.assertEquals(
                kateExpectedBalance,
                kateActualBalance,
                0.01,
                "Kate's balance should not decrease");

        Assertions.assertEquals(
                alexExpectedBalance,
                alexActualBalance,
                0.01,
                "Alex's balance should not increase");
    }

    @Test
    @DisplayName("Проверка невозможности отправки суммы больше чем есть на счёте")
    public void userCannotTransferMoreMoneyThanHaveTest() {
        double kateExpectedBalance = getCurrentBalanceKateSecondAccount();
        double alexExpectedBalance = getCurrentBalanceAlexFirstAccount();

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

        double kateActualBalance = getCurrentBalanceKateSecondAccount();
        double alexActualBalance = getCurrentBalanceAlexFirstAccount();

        Assertions.assertEquals(
                kateExpectedBalance,
                kateActualBalance,
                0.01,
                "Kate's balance should not change");

        Assertions.assertEquals(
                alexExpectedBalance,
                alexActualBalance,
                0.01,
                "Alex's balance should not change");
    }

    @Test
    @DisplayName("Проверка перевода между своими счетами")
    public void userCanTransferMoneyBetweenAccountsTest() {
        double kateFirstAccountExpectedBalance = getCurrentBalanceKateFirstAccount() - 1;
        double kateSecondAccountExpectedBalance = getCurrentBalanceKateSecondAccount() + 1;

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
                .body("amount", equalTo((float) 1.0));

        double kateFirstAccountActualBalance = getCurrentBalanceKateFirstAccount();
        double kateSecondAccountActualBalance = getCurrentBalanceKateSecondAccount();

        Assertions.assertEquals(
                kateFirstAccountExpectedBalance,
                kateFirstAccountActualBalance,
                0.01,
                "Kate's first account balance should decrease by 1");

        Assertions.assertEquals(
                kateSecondAccountExpectedBalance,
                kateSecondAccountActualBalance,
                0.01,
                "Kate's second account balance should increase by 1");
    }

    @Test
    @DisplayName("Проверка невозможности перевода на несуществующий счёт")
    public void userCannotTransferMoneyToNonExistentAccountTest() {
        double kateExpectedBalance = getCurrentBalanceKateFirstAccount();

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

        double kateActualBalance = getCurrentBalanceKateFirstAccount();

        Assertions.assertEquals(
                kateExpectedBalance,
                kateActualBalance,
                0.01,
                "Kate's balance should not change");
    }

    @Test
    @DisplayName("Проверка невозможности перевода без авторизации")
    public void userCannotTransferMoneyWithoutAuthorizationTest() {
        double kateExpectedBalance = getCurrentBalanceKateFirstAccount();
        double alexExpectedBalance = getCurrentBalanceAlexFirstAccount();

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

        double kateActualBalance = getCurrentBalanceKateFirstAccount();
        double alexActualBalance = getCurrentBalanceAlexFirstAccount();

        Assertions.assertEquals(
                kateExpectedBalance,
                kateActualBalance,
                0.01,
                "Kate's balance should not change");

        Assertions.assertEquals(
                alexExpectedBalance,
                alexActualBalance,
                0.01,
                "Alex's balance should not change");
    }

    @Test
    @DisplayName("Проверка перевода от одного клиента другому. Возврат от Алекса к Кейт")
    public void userCanTransferMoneyToSenderTest() {
        double transferAmount = 1111;

        double kateExpectedBalance = getCurrentBalanceKateSecondAccount() + transferAmount;
        double alexExpectedBalance = getCurrentBalanceAlexFirstAccount() - transferAmount;

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", ALEX_AUTH_TOKEN)
                .body("""
                        {
                          "senderAccountId": %s,
                          "receiverAccountId": %s,
                          "amount": %s
                        }
                        """.formatted(ALEX_ACCOUNT_ID_FIRST, KATE_ACCOUNT_ID_SECOND, transferAmount))
                .post("/api/v1/accounts/transfer")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("message", containsString("Transfer successful"))
                .body("amount", equalTo((float) transferAmount));

        double kateActualBalance = getCurrentBalanceKateSecondAccount();
        double alexActualBalance = getCurrentBalanceAlexFirstAccount();

        Assertions.assertEquals(
                kateExpectedBalance,
                kateActualBalance,
                0.01,
                "Kate's balance should increase by " + transferAmount);

        Assertions.assertEquals(
                alexExpectedBalance,
                alexActualBalance,
                0.01,
                "Alex's balance should decrease by " + transferAmount);
    }

    @Test
    @DisplayName("Проверка невозможности перевода с авторизацией на своем аккаунте, но трансфере с чужого аккаунта")
    public void userCannotTransferMoneyFromAnotherUsersAccountTest() {
        double kateExpectedBalance = getCurrentBalanceKateSecondAccount();
        double alexExpectedBalance = getCurrentBalanceAlexFirstAccount();

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

        double kateActualBalance = getCurrentBalanceKateSecondAccount();
        double alexActualBalance = getCurrentBalanceAlexFirstAccount();

        Assertions.assertEquals(
                kateExpectedBalance,
                kateActualBalance,
                0.01,
                "Kate's balance should not change");

        Assertions.assertEquals(
                alexExpectedBalance,
                alexActualBalance,
                0.01,
                "Alex's balance should not change");
    }

    @Test
    @DisplayName("Проверка невозможности перевода со своего аккаунта на тот же аккаунт")
    public void userCannotTransferMoneyToSameAccountTest() {
        double kateExpectedBalance = getCurrentBalanceKateSecondAccount();

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

        double kateActualBalance = getCurrentBalanceKateSecondAccount();

        Assertions.assertEquals(
                kateExpectedBalance,
                kateActualBalance,
                0.01,
                "Kate's balance should not decrease");
    }
}
