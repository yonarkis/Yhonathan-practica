package controlador;

public final class DatabaseConfig {

    private DatabaseConfig() {
    }

    public static String url() {
        return "jdbc:mysql://127.0.0.1:3306/sistema_notas?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static String user() {
        return "root";
    }

    public static String password() {
        return "ROOT";
    }
}
