package models;


import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z0-9]{3,10}$")
    private String username;

    @GeneratingRule(regex = "^[a-z]{2}[A-Z]{2}[0-9]{2}[!@#^&]{1}[a-z]{1}[A-Z]{1}$")
    private String password;

    private String role;
}
