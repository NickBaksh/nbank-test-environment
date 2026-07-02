package iteration_one.api_tests.test_preconditions;

import generators.TestUser;
import iteration_one.api_tests.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest extends BaseTest {

    @Test
    @DisplayName("Проверка, что в системе были созданы аккаунты для Кейт и Алекса, " +
            "а также их счета. Два счёта у Кейт и один у Алекса")
    public void userProfilesAndAccountsWasCreatedTest() {

        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USERS_GET)
                .read()
                .body("find { it.username.startsWith('" + TestUser.KATE.getPrefix() + "') && it.accounts.size() == 2 }", notNullValue())
                .body("find { it.username.startsWith('" + TestUser.ALEX.getPrefix() + "') && it.accounts.size() == 1 }", notNullValue());
    }
}
