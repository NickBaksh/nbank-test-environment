package api.models.dto_model;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomerProfileResponse extends BaseModel {
    private Long id;
    private String username;
    private String name;
    private String role;
}
