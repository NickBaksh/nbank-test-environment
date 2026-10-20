package db;

import api.configs.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public static Connection getConnection() throws SQLException {
        Connection connection = connectionHolder.get();
        if (connection == null || connection.isClosed()) {
            String url = Config.getDbUrl();
            String user = Config.getDbUser();
            String password = Config.getDbPassword();

            connection = DriverManager.getConnection(url, user, password);
            connectionHolder.set(connection);
        }
        return connection;
    }

    public static void closeConnection() {
        Connection connection = connectionHolder.get();
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connectionHolder.remove();
            }
        }
    }
}