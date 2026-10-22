package api.models.dto_model;

import api.generators.GeneratingRule;
import api.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositRequest extends BaseModel {
    @JsonProperty("accountId")
    private Long accountId;

    @GeneratingRule(regex = "^(0\\.0[1-9]|[1-9]\\d{0,3}\\.\\d{2}|5000\\.00)$")
    @JsonProperty("amount")
    private Double amount;
}