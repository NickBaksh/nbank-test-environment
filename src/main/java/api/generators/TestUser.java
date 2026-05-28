package api.generators;

import lombok.Getter;
import api.models.CreateUserRequest;
import api.models.UserRole;

// Enum в котором храню тестовых пользователей по которым создаются аккаунты в системе
@Getter
public enum TestUser {
    KATE("kate", "Kate_"),
    ALEX("alex", "Alex_");

    private final String key;
    private final String prefix;

    TestUser(String key, String prefix) {
        this.key = key;
        this.prefix = prefix;
    }

    public CreateUserRequest createRequest() {
        CreateUserRequest request = RandomModelGenerator.generateWithBuilder(CreateUserRequest.class);
        request.setUsername(prefix + request.getUsername());
        request.setRole(UserRole.USER.toString());
        return request;
    }
}