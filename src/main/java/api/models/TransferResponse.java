package api.models;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse extends BaseModel {
    private Long senderAccountId;
    private Long receiverAccountId;
    private Double amount;
    private String message;
}
