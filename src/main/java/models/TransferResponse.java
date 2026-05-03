package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
