package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import modelo.InscripcionEscolar;

public class InscripcionController {

    public static final List<String> ANIOS_ESTUDIO_VALIDOS = List.of("Primer Año", "Segundo Año", "Tercer Año", "Cuarto Año", "Quinto Año");
    public static final List<String> SECCIONES_VALIDAS = List.of("A", "B", "C", "D");

    private static final List<InscripcionEscolar> inscripciones = new ArrayList<>();

    public static void inscribir(String cedulaEstudiante, String anioEscolar, String anioEstudio, String seccion, String estatus) {
        validar(anioEscolar, anioEstudio, seccion);
        if (buscar(cedulaEstudiante, anioEscolar) != null) {
            throw new IllegalArgumentException("El estudiante ya está inscrito en ese año escolar.");
        }
        inscripciones.add(new InscripcionEscolar(cedulaEstudiante, anioEscolar,
                anioEstudio.trim(), seccion.trim().toUpperCase(Locale.ROOT), estatus == null ? "Regular" : estatus.trim()));
    }

    public static InscripcionEscolar buscar(String cedulaEstudiante, String anioEscolar) {
        InscripcionEscolar local = inscripciones.stream()
                .filter(i -> i.getCedulaEstudiante().equalsIgnoreCase(cedulaEstudiante.trim())
                        && i.getAnioEscolar().equalsIgnoreCase(anioEscolar.trim()))
                .findFirst().orElse(null);
        if (local != null) {
            return local;
        }
        return buscarDesdeDb(cedulaEstudiante, anioEscolar);
    }

    public static InscripcionEscolar buscarEnAnioActivo(String cedulaEstudiante) {
        return buscar(cedulaEstudiante, AnioEscolarController.getAnioActivo());
    }

    public static List<InscripcionEscolar> listarPorSeccion(String anioEscolar, String seccion) {
        List<InscripcionEscolar> locales = inscripciones.stream()
                .filter(i -> i.getAnioEscolar().equalsIgnoreCase(anioEscolar)
                        && i.getSeccion().equalsIgnoreCase(seccion))
                .collect(Collectors.toList());
        if (!locales.isEmpty()) {
            return locales;
        }
        return listarPorSeccionDesdeDb(anioEscolar, seccion);
    }

    public static List<InscripcionEscolar> listarPorAnioEstudioYSeccion(String anioEscolar, String anioEstudio, String seccion) {
        return listar().stream()
                .filter(i -> i.getAnioEscolar().equalsIgnoreCase(anioEscolar)
                        && i.getAnioEstudio().equalsIgnoreCase(anioEstudio)
                        && i.getSeccion().equalsIgnoreCase(seccion))
                .collect(Collectors.toList());
    }

    public static List<InscripcionEscolar> listar() {
        return Collections.unmodifiableList(inscripciones);
    }

    public static void limpiarMemoria() {
        inscripciones.clear();
    }

    private static void validar(String anioEscolar, String anioEstudio, String seccion) {
        if (!anioEscolar.matches("\\d{4}-\\d{4}")) {
            throw new IllegalArgumentException("Año escolar inválido. Use formato 2024-2025.");
        }
        if (!ANIOS_ESTUDIO_VALIDOS.contains(anioEstudio.trim())) {
            throw new IllegalArgumentException("Año de estudio inválido.");
        }
        if (!SECCIONES_VALIDAS.contains(seccion.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Sección inválida.");
        }
    }

    private static InscripcionEscolar buscarDesdeDb(String cedulaEstudiante, String anioEscolar) {
        if (!DatabaseConnection.canConnect()) {
            return null;
        }
        String sql = """
                SELECT i.anio_escolar, gs.grado, gs.letra, i.estatus
                FROM inscripciones i
                JOIN estudiantes e ON e.id_estudiante = i.id_estudiante
                JOIN grados_secciones gs ON gs.id_seccion = i.id_seccion
                WHERE e.cedula = ? AND i.anio_escolar = ?
                LIMIT 1
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cedulaEstudiante.trim());
            ps.setString(2, anioEscolar.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    InscripcionEscolar ins = new InscripcionEscolar(
                            cedulaEstudiante.trim(),
                            rs.getString("anio_escolar"),
                            rs.getString("grado"),
                            rs.getString("letra"),
                            rs.getString("estatus")
                    );
                    inscripciones.add(ins);
                    return ins;
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private static List<InscripcionEscolar> listarPorSeccionDesdeDb(String anioEscolar, String seccion) {
        if (!DatabaseConnection.canConnect()) {
            return Collections.emptyList();
        }
        String sql = """
                SELECT e.cedula, i.anio_escolar, gs.grado, gs.letra, i.estatus
                FROM inscripciones i
                JOIN estudiantes e ON e.id_estudiante = i.id_estudiante
                JOIN grados_secciones gs ON gs.id_seccion = i.id_seccion
                WHERE i.anio_escolar = ? AND gs.letra = ?
                """;
        List<InscripcionEscolar> resultado = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, anioEscolar.trim());
            ps.setString(2, seccion.trim().toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InscripcionEscolar ins = new InscripcionEscolar(
                            rs.getString("cedula"),
                            rs.getString("anio_escolar"),
                            rs.getString("grado"),
                            rs.getString("letra"),
                            rs.getString("estatus")
                    );
                    resultado.add(ins);
                    inscripciones.add(ins);
                }
            }
        } catch (SQLException ignored) {
        }
        return resultado;
    }
}
