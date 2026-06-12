package api.models;

import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerProfileResponse extends BaseModel {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;
}
