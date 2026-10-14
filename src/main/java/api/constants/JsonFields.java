package api.constants;

import lombok.Getter;

public final class JsonFields {
    private JsonFields() {}

    public static final String MESSAGE = "message";
    public static final String STATUS = "status";
    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ROLE = "role";
    public static final String TOKEN = "token";
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String BALANCE = "balance";
    public static final String AMOUNT = "amount";
    public static final String SENDER_ACCOUNT_ID = "senderAccountId";
    public static final String RECEIVER_ACCOUNT_ID = "receiverAccountId";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String FRAUD_RISK_SCORE = "fraudRiskScore";
    public static final String REQUIRES_MANUAL_REVIEW = "requiresManualReview";
    public static final String REQUIRES_VERIFICATION = "requiresVerification";
    public static final String FRAUD_REASON = "fraudReason";
    public static final String RELATED_ACCOUNT = "relatedAccount";
    public static final String RELATED_ACCOUNT_ID = "relatedAccountId";
    public static final String TYPE = "type";
    public static final String TIMESTAMP = "timestamp";
    public static final String TIMESTAMP_AS_STRING = "timestampAsString";
    public static final String AMOUNT_AS_DOUBLE = "amountAsDouble";
    public static final String FRAUD_CHECK_REQUIRED = "fraudCheckRequired";

    public enum Status {
        BLOCKED,
        APPROVED,
        MANUAL_REVIEW_REQUIRED,
        VERIFICATION_REQUIRED;

        @Override
        public String toString() {
            return this.name();
        }
    }

    @Getter
    public enum Message {
        TRANSFER_APPROVED("Transfer approved and processed immediately"),
        TRANSFER_BLOCKED("Transfer blocked due to fraud detection"),
        TRANSFER_REVIEW_REQUIRED("Transfer requires manual review"),
        VERIFICATION_REQUIRED("Additional verification required");

        private final String message;

        Message(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    @Getter
    public enum FraudReason {
        LOW_RISK_TRANSACTION("Low risk transaction"),
        UNEXPECTED_ERROR("Unexpected error during fraud check");
        private final String reason;

        FraudReason(String reason) {
            this.reason = reason;
        }

        @Override
        public String toString() {
            return reason;
        }
    }
}