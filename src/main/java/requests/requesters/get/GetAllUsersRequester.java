package requests.requesters.get;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import requests.GetRequest;

import static io.restassured.RestAssured.given;

public class GetAllUsersRequester extends GetRequest {

    public GetAllUsersRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse get() {
        return given()
                .spec(requestSpecification)
                .get("/api/v1/admin/users")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
