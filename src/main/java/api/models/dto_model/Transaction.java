package api.models.dto_model;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction extends BaseModel {
    private Long id;
    private double amount;
    private double amountAsDouble;
    private String type;
    private String timestamp;
    private String timestampAsString;
    private String status;
    private Boolean fraudCheckRequired;
    private RelatedAccount relatedAccount;
    private Long relatedAccountId;
}
