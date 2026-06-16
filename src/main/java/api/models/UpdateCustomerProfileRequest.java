package api.models;

import api.generators.GeneratingRule;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCustomerProfileRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z]{2,15} [A-Za-z]{2,15}$")
    private String name;
}
