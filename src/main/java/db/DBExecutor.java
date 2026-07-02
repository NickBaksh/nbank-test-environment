package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBExecutor {

    /**
     * Выполнить SELECT запрос и вернуть список объектов
     */
    public static <T> List<T> select(DBRequest<T> request, RowMapper<T> rowMapper) {
        String sql = buildSelectQuery(request);
        List<T> results = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, request);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SELECT query: " + sql, e);
        }

        return results;
    }

    /**
     * Выполнить SELECT запрос и вернуть один объект
     */
    public static <T> T selectOne(DBRequest<T> request, RowMapper<T> rowMapper) {
        List<T> results = select(request, rowMapper);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new RuntimeException("Expected one result, but found " + results.size());
        }
        return results.get(0);
    }

    /**
     * Выполнить INSERT, UPDATE или DELETE запрос
     */
    public static int executeUpdate(DBRequest<?> request) {
        String sql = buildUpdateQuery(request);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, request);
            return stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute update query: " + sql, e);
        }
    }

    /**
     * Построить SELECT запрос
     */
    private static <T> String buildSelectQuery(DBRequest<T> request) {
        StringBuilder sql = new StringBuilder("SELECT ");

        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            sql.append("*");
        } else {
            sql.append(String.join(", ", request.getColumns()));
        }

        sql.append(" FROM ").append(request.getTable());

        if (request.getWhere() != null) {
            sql.append(" WHERE ").append(request.getWhere().getSql());
        }

        if (request.getOrderBy() != null) {
            sql.append(" ORDER BY ").append(request.getOrderBy());
        }

        if (request.getLimit() != null) {
            sql.append(" LIMIT ").append(request.getLimit());
        }

        return sql.toString();
    }

    /**
     * Построить UPDATE запрос (INSERT, UPDATE, DELETE)
     */
    private static String buildUpdateQuery(DBRequest<?> request) {
        return switch (request.getRequestType()) {
            case INSERT -> buildInsertQuery(request);
            case UPDATE -> buildUpdateQueryInternal(request);
            case DELETE -> buildDeleteQuery(request);
            default -> throw new IllegalArgumentException("Unsupported request type: " + request.getRequestType());
        };
    }

    private static String buildInsertQuery(DBRequest<?> request) {
        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Columns must be specified for INSERT");
        }

        String columns = String.join(", ", request.getColumns());
        String placeholders = String.join(", ", request.getColumns().stream()
                .map(c -> "?").toList());

        return "INSERT INTO " + request.getTable() + " (" + columns + ") VALUES (" + placeholders + ")";
    }

    private static String buildUpdateQueryInternal(DBRequest<?> request) {
        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            throw new IllegalArgumentException("Columns must be specified for UPDATE");
        }
        if (request.getWhere() == null) {
            throw new IllegalArgumentException("WHERE condition must be specified for UPDATE");
        }

        String setClause = String.join(", ", request.getColumns().stream()
                .map(c -> c + " = ?").toList());

        return "UPDATE " + request.getTable() + " SET " + setClause + " WHERE " + request.getWhere().getSql();
    }

    private static String buildDeleteQuery(DBRequest<?> request) {
        if (request.getWhere() == null) {
            throw new IllegalArgumentException("WHERE condition must be specified for DELETE");
        }
        return "DELETE FROM " + request.getTable() + " WHERE " + request.getWhere().getSql();
    }

    /**
     * Установить параметры в PreparedStatement
     */
    private static void setParameters(PreparedStatement stmt, DBRequest<?> request) throws SQLException {
        int index = 1;

        // Для WHERE условий
        if (request.getWhere() != null) {
            for (Object param : request.getWhere().getParameters()) {
                stmt.setObject(index++, param);
            }
        }

        // Для UPDATE и INSERT (значения)
        if (request.getValues() != null) {
            for (Object value : request.getValues()) {
                stmt.setObject(index++, value);
            }
        }
    }
}
