package requests.skelethon.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;
import requests.skelethon.Endpoint;
import requests.skelethon.HttpRequest;
import requests.skelethon.interfaces.CrudEndpointInterface;

public class ValidatedCrudRequester<M extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    private CrudRequester crudRequester;

    public ValidatedCrudRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification, Endpoint endpoint) {
        super(requestSpecification, responseSpecification, endpoint);
        this.crudRequester = new CrudRequester(requestSpecification, responseSpecification, endpoint);
    }

    @Override
    public M create(BaseModel model) {
        return (M) crudRequester.create(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M create() {
        return (M) crudRequester.create().extract().as(endpoint.getResponseModel());
    }

    @Override
    public M read() {
        return (M) crudRequester.read().extract().as(endpoint.getResponseModel());
    }

    @Override
    public M read(long id) {
        return (M) crudRequester.read(id).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M update(BaseModel model) {
        return (M) crudRequester.update(model).extract().as(endpoint.getResponseModel());
    }

    @Override
    public M delete(long id) {
        return (M) crudRequester.delete(id).extract().as(endpoint.getResponseModel());
    }
}
