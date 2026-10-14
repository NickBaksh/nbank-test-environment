package db;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class DBRequest<T> {
    private RequestType requestType;
    private String table;
    private Condition where;
    private List<String> columns;
    private List<Object> values;
    private Class<T> extractAs;
    private String orderBy;
    private Integer limit;

    // Вспомогательный класс для построения запросов
    public static class DBRequestBuilder<T> {
        private RequestType requestType;
        private String table;
        private Condition where;
        private List<String> columns = new ArrayList<>();
        private List<Object> values = new ArrayList<>();
        private Class<T> extractAs;
        private String orderBy;
        private Integer limit;

        public DBRequestBuilder<T> requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public DBRequestBuilder<T> table(String table) {
            this.table = table;
            return this;
        }

        public DBRequestBuilder<T> where(Condition condition) {
            this.where = condition;
            return this;
        }

        public DBRequestBuilder<T> columns(String... columns) {
            this.columns = List.of(columns);
            return this;
        }

        public DBRequestBuilder<T> values(Object... values) {
            this.values = List.of(values);
            return this;
        }

        public DBRequestBuilder<T> extractAs(Class<T> clazz) {
            this.extractAs = clazz;
            return this;
        }

        public DBRequestBuilder<T> orderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        public DBRequestBuilder<T> limit(int limit) {
            this.limit = limit;
            return this;
        }

        public DBRequest<T> build() {
            return new DBRequest<>(requestType, table, where, columns, values, extractAs, orderBy, limit);
        }
    }

    public List<T> execute(RowMapper<T> rowMapper) {
        if (requestType == RequestType.SELECT) {
            return DBExecutor.select(this, rowMapper);
        } else {
            throw new UnsupportedOperationException("Use executeUpdate() for non-SELECT queries");
        }
    }

    public T executeOne(RowMapper<T> rowMapper) {
        return DBExecutor.selectOne(this, rowMapper);
    }

    public int executeUpdate() {
        return DBExecutor.executeUpdate(this);
    }
}
