package api.specs;

import api.configs.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import api.models.LoginUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;

import java.util.List;

public class RequestSpecs {
    public static final Double TRANSACTION_0_0_1 = 0.01;
    public static final Double TRANSACTION_0 = 0.00;
    public static final Double TRANSACTION_1 = 1.00;
    public static final Double TRANSACTION_100 = 100.00;
    public static final Double TRANSACTION_1000 = 1000.00;
    public static final Double TRANSACTION_10000 = 10000.00;
    public static final Double TRANSACTION_10000_0_1 = 10000.01;
    public static final Double BALANCE_5000 = 5000.00;

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaultRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(Config.getProperty("apiBaseUrl") + Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unauthSpec() {
        return defaultRequestBuilder()
                .build();
    }

    public static RequestSpecification adminSpec() {
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification authAsUserSpec(String username, String password) {
        String userAuthHeader = new CrudRequester(
                RequestSpecs.unauthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN)
                .create(LoginUserRequest.builder().username(username).password(password).build())
                .extract()
                .header("Authorization");

        return defaultRequestBuilder()
                .addHeader("Authorization", userAuthHeader)
                .build();
    }

    public static RequestSpecification authWithTokenSpec(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null. Make sure user is authenticated.");
        }
        return defaultRequestBuilder()
                .addHeader("Authorization", token)
                .build();
    }
}