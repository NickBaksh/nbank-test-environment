package api.requests.skelethon.requesters;

import api.configs.Config;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;
import api.requests.skelethon.interfaces.GetAllEndpointInterface;
import common.helpers.StepLogger;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {

    public CrudRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, Endpoint endpoint) {
        super(requestSpecification, responseSpecification, endpoint);
    }

    @Override
    public ValidatableResponse create(BaseModel model) {
        return StepLogger.log("POST request to " + fullUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    public ValidatableResponse create() {
        return create(null);
    }

    public Response createResponse(BaseModel model) {
        return StepLogger.log("POST request to " + fullUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification)
                    .extract()
                    .response();
        });
    }

    @Override
    public ValidatableResponse read() {
        return StepLogger.log("GET request to " + fullUrl(), () -> given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification));
    }

    @Override
    public ValidatableResponse read(long id) {
        return StepLogger.log("GET request to " + fullUrl(id), () -> given()
                .spec(requestSpecification)
                .pathParam(endpoint.getPathParam(), id)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification));
    }

    public Response readResponse(long id) {
        return StepLogger.log("GET request to " + fullUrl(id), () -> given()
                .spec(requestSpecification)
                .pathParam(endpoint.getPathParam(), id)
                .get(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification)
                .extract()
                .response());
    }

    @Override
    public ValidatableResponse update(BaseModel model) {
        return StepLogger.log("PUT request to " + fullUrl(), () -> given()
                .spec(requestSpecification)
                .body(model)
                .put(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification));
    }

    @Override
    public ValidatableResponse delete(long id) {
        return StepLogger.log("DELETE request to " + fullUrl(id), () -> given()
                .spec(requestSpecification)
                .pathParam(endpoint.getPathParam(), id)
                .delete(endpoint.getUrl())
                .then()
                .assertThat()
                .spec(responseSpecification));
    }

    @Override
    public ValidatableResponse getAll(Class<?> clazz) {
        return StepLogger.log("GET request to " + fullUrl(), () -> given()
                .spec(requestSpecification)
                .get(endpoint.getUrl())
                .then().assertThat()
                .spec(responseSpecification));
    }

    // Полный путь для названия шага в Allure: /api/v1 + /accounts/deposit.
    // Сам запрос идет на endpoint.getUrl(): apiPath и версия уже в baseUri
    private String fullUrl() {
        return Config.getProperty("apiPath") + "/" + Config.getApiVersion() + endpoint.getUrl();
    }

    private String fullUrl(long id) {
        return fullUrl() + " (" + endpoint.getPathParam() + "=" + id + ")";
    }
}
