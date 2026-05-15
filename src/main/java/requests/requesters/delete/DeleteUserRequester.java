package requests.requesters.delete;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import requests.DeleteRequest;

import static io.restassured.RestAssured.given;

public class DeleteUserRequester extends DeleteRequest {
    private final int userId;

    public DeleteUserRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, int userId) {
        super(requestSpecification, responseSpecification);
        this.userId = userId;
    }

    @Override
    public ValidatableResponse delete() {
        return given()
                .spec(requestSpecification)
                .pathParam("id", userId)
                .delete("/api/v1/admin/users/{id}")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }
}
