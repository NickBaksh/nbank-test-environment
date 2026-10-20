package api.models.dto_model;


import api.generators.GeneratingRule;
import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z0-9]{3,5}$")
    private String username;

    @GeneratingRule(regex = "^[a-z]{2}[A-Z]{2}[0-9]{2}[!@#^&]{1}[a-z]{1}[A-Z]{1}$")
    private String password;

    private String role;

    public static CreateUserRequest getAdmin() {
        return CreateUserRequest.builder().username(api.configs.Config.getProperty("admin.username"))
                .password(api.configs.Config.getProperty("admin.password"))
                .build();
    }
}
