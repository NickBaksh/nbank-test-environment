package api.models.dto_model;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositResponse extends BaseModel {
    private Long id;
    private String accountNumber;
    private double balance;
    private double depositAmount;
    private Long transactionId;
}
