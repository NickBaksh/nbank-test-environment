package iteration_one.deposit_test_cases;

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

public class DepositOperationsTest extends BaseTest {

    @ParameterizedTest
    @CsvSource(
            {
                    "-0.01, 400",
                    "0, 400"
            }
    )
    @DisplayName("Проверка невозможности разместить невалидную сумму на счёте. 0 < депозит <= 5000")
    public void userCanNotDepositInvalidSumTest(double deposit, int expectedStatusCode) {
        double balanceBefore = getCurrentBalanceKateFirstAccount();

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "id": %s,
                          "balance": %s
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST, deposit))
                .post("/api/v1/accounts/deposit")
                .then()
                .log().all()
                .statusCode(expectedStatusCode)
                .body(equalTo("Deposit amount must be at least 0.01"));

        double balanceAfter = getCurrentBalanceKateFirstAccount();

        Assertions.assertEquals(
                balanceBefore,
                balanceAfter,
                0.01,
                "Balance should not change");
    }

    @Test
    @DisplayName("Проверка невозможности разместить сумму больше 5000 на счёте. 0 < депозит <= 5000")
    public void userCanNotDepositSumAbove5000Test() {
        double balanceBefore = getCurrentBalanceKateFirstAccount();

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "id": %s,
                          "balance": 5000.01
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST))
                .post("/api/v1/accounts/deposit")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(equalTo("Deposit amount cannot exceed 5000"));

        double balanceAfter = getCurrentBalanceKateFirstAccount();
        Assertions.assertEquals(
                balanceBefore,
                balanceAfter,
                0.01,
                "Balance should not change");
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "0.01, 200",
                    "4999.99, 200",
                    "5000, 200"
            }
    )
    @DisplayName("Проверка размещения на депозите валидных сумм. 0 < депозит <= 5000")
    public void userCanDepositValidSumTest(double deposit, int expectedStatusCode) {
        double balanceBefore = getCurrentBalanceKateFirstAccount();
        double expectedBalance = balanceBefore + deposit;

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "id": %s,
                          "balance": %s
                        }
                        """.formatted(KATE_ACCOUNT_ID_FIRST, deposit))
                .post("/api/v1/accounts/deposit")
                .then()
                .log().all()
                .statusCode(expectedStatusCode);

        double balanceAfter = getCurrentBalanceKateFirstAccount();

        Assertions.assertEquals(
                expectedBalance,
                balanceAfter,
                0.01,
                "Balance should increase by " + deposit);
    }

    @Test
    @DisplayName("Проверка невозможности разместить депозит на чужом аккаунте")
    public void userCannotDepositToAnotherUsersAccountsTest() {
        double balanceBefore = getCurrentBalanceKateFirstAccount();

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "id": %s,
                          "balance": 1
                        }
                        """.formatted(ALEX_ACCOUNT_ID_FIRST))
                .post("/api/v1/accounts/deposit")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(equalTo("Unauthorized access to account"));

        double balanceAfter = getCurrentBalanceKateFirstAccount();
        Assertions.assertEquals(
                balanceBefore,
                balanceAfter,
                0.01,
                "Balance should not change");
    }

    @Test
    @DisplayName("Проверка невозможности разместить депозит на несуществующем аккаунте")
    public void cannotDepositToNonExistentAccountTest() {
        double balanceBefore = getCurrentBalanceKateFirstAccount();

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", KATE_AUTH_TOKEN)
                .body("""
                        {
                          "id": 999,
                          "balance": 1
                        }
                        """)
                .post("/api/v1/accounts/deposit")
                .then()
                .log().all()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(equalTo("Unauthorized access to account"));

        double balanceAfter = getCurrentBalanceKateFirstAccount();
        Assertions.assertEquals(
                balanceBefore,
                balanceAfter,
                0.01,
                "Balance should not change");
    }
}
