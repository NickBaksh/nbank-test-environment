package db;

import lombok.Getter;

@Getter
public enum Tables {
    CUSTOMERS("customers"),
    ACCOUNTS("accounts"),
    TRANSACTIONS("transactions");

    private final String tableName;
    Tables(String tableName) {
        this.tableName = tableName;
    }
}
