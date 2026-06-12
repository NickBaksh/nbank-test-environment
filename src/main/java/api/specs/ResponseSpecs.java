package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;

public class ResponseSpecs {
    private ResponseSpecs() {}
    // Константы с сообщениями в response
    // Сообщения об ошибках
    public static final String DEPOSIT_AMOUNT_MIN_ERROR = "Deposit amount must be at least 0.01";
    public static final String DEPOSIT_AMOUNT_MAX_ERROR = "Deposit amount cannot exceed 5000";
    public static final String TRANSFER_AMOUNT_MIN_ERROR = "Transfer amount must be at least 0.01";
    public static final String TRANSFER_AMOUNT_MAX_ERROR = "Transfer amount cannot exceed 10000";
    public static final String UNAUTHORIZED_ACCESS_ERROR = "Unauthorized access to account";
    public static final String INSUFFICIENT_FUNDS_ERROR = "Invalid transfer: insufficient funds or invalid accounts";
    public static final String PROFILE_NAME_FORMAT_ERROR = "Name must contain two words with letters only";
    // Успешные сообщения
    public static final String PROFILE_UPDATE_SUCCESS = "Profile updated successfully";
    public static final String TRANSFER_SUCCESS = "Transfer successful";


    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification returnsBadRequest() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .build();
    }

    public static ResponseSpecification returnsInternalServerError() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .build();
    }

    public static ResponseSpecification returnsForbidden() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .build();
    }

    public static ResponseSpecification returnsUnauthorize() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .build();
    }
}
