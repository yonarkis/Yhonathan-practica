package controlador;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static String lastError = "Sin intento de conexión";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.url(), DatabaseConfig.user(), DatabaseConfig.password());
    }

    public static boolean canConnect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection ignored = getConnection()) {
                lastError = "Conexión OK";
                return true;
            }
        } catch (ClassNotFoundException ex) {
            lastError = "Falta driver MySQL (mysql-connector-j) en librerías del proyecto.";
            return false;
        } catch (SQLException ex) {
            lastError = ex.getMessage();
            return false;
        }
    }

    public static String getLastError() {
        return lastError;
    }
}
