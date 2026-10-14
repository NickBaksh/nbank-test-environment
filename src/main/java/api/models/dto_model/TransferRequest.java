package api.models.dto_model;

import api.models.BaseModel;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest extends BaseModel {
    private Long senderAccountId;
    private Long receiverAccountId;
    private Double amount;
}
