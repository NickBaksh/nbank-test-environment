package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.*;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

public class Steps {
    protected SoftAssertions softly;

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    protected static void deleteAllUsers() {
        // Получаем список всех пользователей
        ValidatedCrudRequester<GetAllUsersResponse> getAllUsers = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USERS_GET
        );

        GetAllUsersResponse allUsersResponse = getAllUsers.read();
        List<Customer> allUsers = allUsersResponse.getCustomers();

        // Удаляем тестовых пользователей
        CrudRequester deleteRequester = new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USER_DELETE
        );

        for (Customer user : allUsers) {
            String username = user.getUsername();
            if (username != null && (username.startsWith("Kate") || username.startsWith("Alex"))) {
                deleteRequester.delete(user.getId());
            }
        }
    }

    protected static String createUserAndGetToken() {
        // Генерируем случайные логин и пароль пользователя
        CreateUserRequest request = RandomModelGenerator.generateWithBuilder(CreateUserRequest.class);
        request.setUsername(request.getUsername());
        request.setRole(UserRole.USER.toString());

        // Собираем запрос для создания пользователя
        ValidatedCrudRequester<CreateUserResponse> requester = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ADMIN_USER_CREATE
        );

        // Отправляем запрос на создание пользователя
        requester.create(request);

        // Логинимся и получаем токен
        LoginUserRequest loginUserRequest = LoginUserRequest.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .build();

        CrudRequester loginRequester = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN
        );

        return loginRequester.create(loginUserRequest)
                .extract()
                .header("Authorization");
    }
}
