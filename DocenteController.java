package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Docente;

public class DocenteController {

    private static final List<Docente> docentes = new ArrayList<>();

    static {
        docentes.add(new Docente(1, "docente", "1234", "CARLOS PÉREZ", Docente.ROL_DOCENTE));
        docentes.add(new Docente(2, "control", "1234", "MARÍA CONTROL", Docente.ROL_CONTROL_ESTUDIOS));
        docentes.add(new Docente(3, "director", "1234", "ANA RAMÍREZ", Docente.ROL_DIRECTOR));
        docentes.add(new Docente(4, "admin", "admin123", "ADMINISTRADOR", Docente.ROL_ADMIN));

        docentes.add(new Docente(5, "doc_mat_1", "1234", "LUIS MATEMÁTICAS", Docente.ROL_DOCENTE));
        docentes.add(new Docente(6, "doc_len_1", "1234", "SOFÍA LENGUA", Docente.ROL_DOCENTE));
        docentes.add(new Docente(7, "doc_soc_1", "1234", "MIGUEL HISTORIA", Docente.ROL_DOCENTE));
        docentes.add(new Docente(8, "doc_cie_1", "1234", "ANDREA CIENCIAS", Docente.ROL_DOCENTE));
        docentes.add(new Docente(9, "doc_mix_1", "1234", "JOSÉ INTEGRAL", Docente.ROL_DOCENTE));
        docentes.add(new Docente(10, "doc_mix_2", "1234", "DANIELA INTEGRAL", Docente.ROL_DOCENTE));
        docentes.add(new Docente(11, "doc_fis_1", "1234", "RAMÓN FÍSICA", Docente.ROL_DOCENTE));
        docentes.add(new Docente(12, "doc_qui_1", "1234", "ELENA QUÍMICA", Docente.ROL_DOCENTE));
    }

    public static Docente login(String usuario, String clave) {
        Docente dbDocente = loginFromDb(usuario, clave);
        if (dbDocente != null) {
            return dbDocente;
        }

        for (Docente d : docentes) {
            if (d.getUsuario().equalsIgnoreCase(usuario.trim()) && d.getClave().equals(clave.trim())) {
                return d;
            }
        }
        return null;
    }

    public static List<Docente> listar() {
        return Collections.unmodifiableList(docentes);
    }

    public static Docente buscarPorId(int id) {
        return docentes.stream().filter(d -> d.getId() == id).findFirst().orElse(null);
    }

    private static Docente loginFromDb(String usuario, String clave) {
        String sql = """
                SELECT u.id_usuario, u.username, u.password_hash, u.rol,
                       COALESCE(d.nombre, 'Usuario') AS nombre,
                       COALESCE(d.apellido, '') AS apellido
                FROM usuarios u
                LEFT JOIN docentes d ON d.id_usuario = u.id_usuario
                WHERE u.username = ? AND u.password_hash = ? AND u.activo = 1
                """;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario.trim());
            ps.setString(2, clave.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String rol = rs.getString("rol").toLowerCase();
                    String nombre = (rs.getString("nombre") + " " + rs.getString("apellido")).trim();
                    return new Docente(rs.getInt("id_usuario"), rs.getString("username"), rs.getString("password_hash"), nombre, rol);
                }
            }
        } catch (SQLException ignored) {
            // fallback memoria
        }
        return null;
    }
}
