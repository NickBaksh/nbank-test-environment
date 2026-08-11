package api.models.dto_model;

import api.models.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferWithFraudCheckResponse extends BaseModel {
    private Long senderAccountId;
    private Long receiverAccountId;
    private Double amount;
    private String status;
    private String message;
    private Long transactionId;
    private Double fraudRiskScore;
    private Boolean requiresManualReview;
    private Boolean requiresVerification;
    private String fraudReason;
}