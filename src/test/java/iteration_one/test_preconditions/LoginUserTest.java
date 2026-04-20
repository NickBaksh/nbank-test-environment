package iteration_one.test_preconditions;

import iteration_one.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import requests.requesters.get.GetAllUsersRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest extends BaseTest {

    @Test
    @DisplayName("Проверка, что в системе были созданы аккаунты для Кейт и Алекса, " +
            "а также их счета. Два счёта у Кейт и один у Алекса")
    public void userProfilesAndAccountsWasCreatedTest() {

        new GetAllUsersRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK())
                .get()
                .body("find { it.username.startsWith('Kate_') && it.accounts.size() == 2 }", notNullValue())
                .body("find { it.username.startsWith('Alex_') && it.accounts.size() == 1 }", notNullValue());

    }
}
