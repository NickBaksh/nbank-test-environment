package api.models;

import api.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositRequest extends BaseModel {
    private Long id;

    @GeneratingRule(regex = "^(0\\.0[1-9]|[1-9]\\d{0,3}\\.\\d{2}|5000\\.00)$")
    private double balance;
}