package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.dto_model.CreateUserRequest;
import api.models.dto_model.CreateUserResponse;
import api.models.dto_model.Customer;
import api.models.dto_model.GetAllUsersResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class AdminSteps {
    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ADMIN_USER_CREATE)
                .create(userRequest);

        return userRequest;
    }

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USERS_GET)
                .getAll(CreateUserResponse[].class);
    }

    public static void deleteUser(long userId) {
        CrudRequester deleteRequester = new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USER_DELETE
        );
        deleteRequester.delete(userId);
    }

    public static void deleteAllTestUsers() {
        ValidatedCrudRequester<GetAllUsersResponse> getAllUsers = new ValidatedCrudRequester<>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USERS_GET
        );

        GetAllUsersResponse response = getAllUsers.read();
        CrudRequester deleteRequester = new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.ADMIN_USER_DELETE
        );

        for (Customer user : response.getCustomers()) {
            if (user.getUsername() != null && user.getUsername().startsWith("Test_")) {
                deleteRequester.delete(user.getId());
            }
        }
    }
}

