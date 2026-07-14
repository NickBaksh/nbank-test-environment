package db;

import lombok.Getter;

@Getter
public enum RequestType {
    SELECT("SELECT"),
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String requestType;
    RequestType(String type) {
        this.requestType = type;
    }
}
