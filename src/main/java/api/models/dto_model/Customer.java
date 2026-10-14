package api.models.dto_model;

import api.models.BaseModel;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer extends BaseModel {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;
}
