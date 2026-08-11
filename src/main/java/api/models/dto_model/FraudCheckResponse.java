package api.models.dto_model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FraudCheckResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("decision")
    private String decision;

    @JsonProperty("riskScore")
    private Double riskScore;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("requiresManualReview")
    private Boolean requiresManualReview;

    @JsonProperty("additionalVerificationRequired")
    private Boolean additionalVerificationRequired;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("fraudRules")
    private List<String> fraudRules;

    @JsonProperty("recommendedAction")
    private String recommendedAction;

    public FraudCheckResponse() {
    }

    public FraudCheckResponse(String status, String decision, Double riskScore,
                              String reason, Boolean requiresManualReview,
                              Boolean additionalVerificationRequired) {
        this.status = status;
        this.decision = decision;
        this.riskScore = riskScore;
        this.reason = reason;
        this.requiresManualReview = requiresManualReview;
        this.additionalVerificationRequired = additionalVerificationRequired;
    }

    // Вспомогательные методы
    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(decision);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(decision);
    }

    public boolean isManualReviewRequired() {
        return Boolean.TRUE.equals(requiresManualReview);
    }

    public boolean isHighRisk() {
        return riskScore != null && riskScore > 0.7;
    }

    @Override
    public String toString() {
        return "FraudCheckResponse{" +
                "status='" + status + '\'' +
                ", decision='" + decision + '\'' +
                ", riskScore=" + riskScore +
                ", reason='" + reason + '\'' +
                ", requiresManualReview=" + requiresManualReview +
                ", additionalVerificationRequired=" + additionalVerificationRequired +
                ", transactionId='" + transactionId + '\'' +
                '}';
    }
}