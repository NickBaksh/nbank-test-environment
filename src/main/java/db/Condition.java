package db;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Condition {
    private final String sql;
    private final List<Object> parameters;

    private Condition(String sql, List<Object> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public static Condition equalTo(String column, Object value) {
        return new Condition(column + " = ?", List.of(value));
    }

    public static Condition notEqualTo(String column, Object value) {
        return new Condition(column + " != ?", List.of(value));
    }

    public static Condition greaterThan(String column, Object value) {
        return new Condition(column + " > ?", List.of(value));
    }

    public static Condition lessThan(String column, Object value) {
        return new Condition(column + " < ?", List.of(value));
    }

    public static Condition like(String column, String pattern) {
        return new Condition(column + " LIKE ?", List.of(pattern));
    }

    public static Condition isNull(String column) {
        return new Condition(column + " IS NULL", List.of());
    }

    public static Condition isNotNull(String column) {
        return new Condition(column + " IS NOT NULL", List.of());
    }

    public Condition and(Condition other) {
        String combinedSql = this.sql + " AND " + other.sql;
        List<Object> combinedParams = new ArrayList<>();
        combinedParams.addAll(this.parameters);
        combinedParams.addAll(other.parameters);
        return new Condition(combinedSql, combinedParams);
    }

    public Condition or(Condition other) {
        String combinedSql = "(" + this.sql + ") OR (" + other.sql + ")";
        List<Object> combinedParams = new ArrayList<>();
        combinedParams.addAll(this.parameters);
        combinedParams.addAll(other.parameters);
        return new Condition(combinedSql, combinedParams);
    }

}
