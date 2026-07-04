package api.models.dto_model;

import api.models.BaseModel;
import api.models.UserRole;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginUserResponse extends BaseModel {
    private UserRole role;
    private String username;
}
