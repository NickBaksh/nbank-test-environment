package requests.requesters.post;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.EmptyRequest;
import requests.PostRequest;

import static io.restassured.RestAssured.given;

public class CreateUserAccountRequester extends PostRequest<EmptyRequest> {
    public CreateUserAccountRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(EmptyRequest model) {
        return given()
                .spec(requestSpecification)
                .post("/api/v1/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
