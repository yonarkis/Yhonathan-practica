package controlador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import modelo.Estudiante;

public class EstudianteController {

    public static final List<String> SECCIONES_VALIDAS = List.of("A", "B", "C", "D");
    private static final List<Estudiante> estudiantes = new ArrayList<>();
    private static int contadorId = 1;

    static {
        generarDatosPruebaMemoria();
    }

    public static void agregar(String cedula, String nombre, String apellido) {
        agregar(nombre, "", apellido, "", cedula, "", "Primer Año", "A");
    }

    public static void agregar(String nombre, String segundoNombre, String apellido, String segundoApellido,
            String cedula, String fechaNacimiento, String grado, String seccion) {
        validarCampos(nombre, apellido, cedula, seccion);
        String nombreU = uppercase(nombre);
        String segundoNombreU = uppercase(segundoNombre);
        String apellidoU = uppercase(apellido);
        String segundoApellidoU = uppercase(segundoApellido);

        if (buscarPorCedula(cedula) != null) {
            throw new IllegalArgumentException("La cédula ya está registrada.");
        }

        Estudiante estudiante = new Estudiante(contadorId++, nombreU, segundoNombreU, apellidoU,
                segundoApellidoU, cedula.trim(), fechaNacimiento == null ? "" : fechaNacimiento.trim(),
                grado.trim(), seccion.trim().toUpperCase(Locale.ROOT));
        estudiantes.add(estudiante);

        InscripcionController.inscribir(cedula.trim(), AnioEscolarController.getAnioActivo(), grado.trim(), seccion.trim(), "Regular");
    }

    public static void registrarCompleto(String nombre, String segundoNombre, String apellido, String segundoApellido,
            String cedula, String fechaNacimiento, String anioEstudio, String seccion,
            String cedulaMadre, String nombreMadre, String apellidoMadre,
            String cedulaRepresentante, String nombreRepresentante, String apellidoRepresentante, String parentescoRepresentante) {

        validarCampos(nombre, apellido, cedula, seccion);
        RepresentanteController.registrar(cedula, cedulaMadre, nombreMadre, apellidoMadre,
                cedulaRepresentante, nombreRepresentante, apellidoRepresentante, parentescoRepresentante);

        if (DatabaseConnection.canConnect()) {
            registrarEnDb(nombre, segundoNombre, apellido, segundoApellido, cedula, fechaNacimiento,
                    anioEstudio, seccion, cedulaMadre, nombreMadre, apellidoMadre);
        }

        if (buscarPorCedula(cedula) == null) {
            agregar(nombre, segundoNombre, apellido, segundoApellido, cedula, fechaNacimiento, anioEstudio, seccion);
        }
    }

    public static List<Estudiante> listar() {
        return Collections.unmodifiableList(estudiantes);
    }

    public static List<Estudiante> listarPorSeccion(String seccion) {
        String anio = AnioEscolarController.getAnioActivo();
        return InscripcionController.listarPorSeccion(anio, seccion).stream()
                .map(i -> buscarPorCedula(i.getCedulaEstudiante()))
                .filter(e -> e != null)
                .collect(Collectors.toList());
    }

    public static List<Estudiante> listarPorAnioYSeccion(String anioEstudio, String seccion) {
        String anio = AnioEscolarController.getAnioActivo();
        return InscripcionController.listarPorAnioEstudioYSeccion(anio, anioEstudio, seccion).stream()
                .map(i -> buscarPorCedula(i.getCedulaEstudiante()))
                .filter(e -> e != null)
                .collect(Collectors.toList());
    }

    public static Estudiante buscarPorId(int id) {
        return estudiantes.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
    }

    public static Estudiante buscarPorCedula(String cedula) {
        Estudiante local = estudiantes.stream()
                .filter(e -> e.getCedula().equalsIgnoreCase(cedula.trim()))
                .findFirst().orElse(null);
        if (local != null) {
            return local;
        }
        return buscarDesdeDb(cedula);
    }

    public static void actualizar(String cedula, String nombre, String segundoNombre, String apellido,
            String segundoApellido, String fechaNacimiento, String grado, String seccion) {
        validarCampos(nombre, apellido, cedula, seccion);
        Estudiante estudiante = buscarPorCedula(cedula);
        if (estudiante == null) {
            throw new IllegalArgumentException("No existe estudiante con esa cédula.");
        }
        estudiante.actualizar(uppercase(nombre), uppercase(segundoNombre), uppercase(apellido), uppercase(segundoApellido),
                fechaNacimiento == null ? "" : fechaNacimiento.trim(), grado.trim(), seccion.trim().toUpperCase(Locale.ROOT));

        String anio = AnioEscolarController.getAnioActivo();
        modelo.InscripcionEscolar inscripcion = InscripcionController.buscar(cedula, anio);
        if (inscripcion == null) {
            InscripcionController.inscribir(cedula, anio, grado, seccion, "Regular");
        } else {
            inscripcion.actualizar(grado, seccion, inscripcion.getEstatus());
        }
    }

    public static void eliminar(String cedula) {
        Estudiante estudiante = buscarPorCedula(cedula);
        if (estudiante == null) {
            throw new IllegalArgumentException("No existe estudiante con esa cédula.");
        }
        NotaController.eliminarNotasPorEstudiante(estudiante.getId());
        estudiantes.remove(estudiante);
    }



    public static void limpiarDatosMemoria() {
        estudiantes.clear();
        contadorId = 1;
    }

    public static void regenerarDatosDemo() {
        limpiarDatosMemoria();
        generarDatosPruebaMemoria();
    }

    private static void generarDatosPruebaMemoria() {
        if (!estudiantes.isEmpty()) {
            return;
        }
        String[] nombres = {"LUIS", "MARÍA", "JOSÉ", "ANA", "CARLOS", "SOFÍA", "MIGUEL", "ELENA", "DANIEL", "PAULA",
            "ANDRÉS", "LUCÍA", "RAMÓN", "CAMILA", "JHON", "VALENTINA", "PEDRO", "ISABEL", "GABRIEL", "ROSA"};
        String[] apellidos = {"PÉREZ", "GARCÍA", "RODRÍGUEZ", "FERNÁNDEZ", "GONZÁLEZ", "LÓPEZ", "MARTÍNEZ", "DÍAZ", "RAMÍREZ", "TORRES"};

        int cedula = 15000000;
        for (String anio : InscripcionController.ANIOS_ESTUDIO_VALIDOS) {
            for (String seccion : InscripcionController.SECCIONES_VALIDAS) {
                for (int i = 0; i < 10; i++) {
                    String nombre = nombres[(i + anio.length()) % nombres.length];
                    String segundoNombre = (i % 2 == 0) ? nombres[(i + 3) % nombres.length] : "";
                    String apellido = apellidos[(i + seccion.charAt(0)) % apellidos.length];
                    String segundoApellido = (i % 3 == 0) ? apellidos[(i + 4) % apellidos.length] : "";
                    agregar(nombre, segundoNombre, apellido, segundoApellido,
                            String.valueOf(cedula++), "", anio, seccion);
                }
            }
        }
    }

    private static Estudiante buscarDesdeDb(String cedula) {
        if (!DatabaseConnection.canConnect()) {
            return null;
        }
        String sql = """
                SELECT e.id_estudiante, e.cedula, e.nombre, e.apellido
                FROM estudiantes e
                WHERE e.cedula = ?
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cedula.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Estudiante e = new Estudiante(rs.getInt("id_estudiante"), rs.getString("nombre"), "",
                            rs.getString("apellido"), "", rs.getString("cedula"), "", "", "A");
                    estudiantes.add(e);
                    return e;
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private static void registrarEnDb(String nombre, String segundoNombre, String apellido, String segundoApellido,
            String cedula, String fechaNacimiento, String anioEstudio, String seccion,
            String cedulaMadre, String nombreMadre, String apellidoMadre) {

        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);

            int idRepresentante = obtenerOCrearRepresentante(c, cedulaMadre, nombreMadre, apellidoMadre);
            int idEstudiante = obtenerOCrearEstudiante(c, idRepresentante, nombre, apellido, cedula, fechaNacimiento);
            int idSeccion = obtenerIdSeccion(c, anioEstudio, seccion);
            insertarInscripcionSiNoExiste(c, idEstudiante, idSeccion, AnioEscolarController.getAnioActivo());

            c.commit();
        } catch (SQLException ex) {
            throw new IllegalArgumentException("Error guardando en BD: " + ex.getMessage());
        }
    }

    private static int obtenerOCrearRepresentante(Connection c, String cedula, String nombre, String apellido) throws SQLException {
        String find = "SELECT id_representante FROM representantes WHERE cedula = ?";
        try (PreparedStatement ps = c.prepareStatement(find)) {
            ps.setString(1, cedula.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        try {
            return insertarRepresentanteEsquemaNuevo(c, cedula, nombre, apellido);
        } catch (SQLException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(Locale.ROOT);
            if (!msg.contains("unknown column") || (!msg.contains("nombre") && !msg.contains("apellido"))) {
                throw ex;
            }
            return insertarRepresentanteEsquemaLegado(c, cedula, nombre, apellido);
        }
    }

    private static int insertarRepresentanteEsquemaNuevo(Connection c, String cedula, String nombre, String apellido) throws SQLException {
        String insert = "INSERT INTO representantes (nombre, apellido, parentesco, cedula) VALUES (?, ?, 'MADRE', ?)";
        try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, uppercase(nombre));
            ps.setString(2, uppercase(apellido));
            ps.setString(3, cedula.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static int insertarRepresentanteEsquemaLegado(Connection c, String cedula, String nombre, String apellido) throws SQLException {
        String insert = "INSERT INTO representantes (nombre_completo, parentesco, cedula) VALUES (?, 'MADRE', ?)";
        try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, (uppercase(nombre) + " " + uppercase(apellido)).trim());
            ps.setString(2, cedula.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static int obtenerOCrearEstudiante(Connection c, int idRepresentante, String nombre, String apellido,
            String cedula, String fechaNacimiento) throws SQLException {
        String find = "SELECT id_estudiante FROM estudiantes WHERE cedula = ?";
        try (PreparedStatement ps = c.prepareStatement(find)) {
            ps.setString(1, cedula.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        String insert = "INSERT INTO estudiantes (id_representante, nombre, apellido, cedula, fecha_nac) VALUES (?, ?, ?, ?, NULL)";
        try (PreparedStatement ps = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idRepresentante);
            ps.setString(2, uppercase(nombre));
            ps.setString(3, uppercase(apellido));
            ps.setString(4, cedula.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static int obtenerIdSeccion(Connection c, String grado, String seccion) throws SQLException {
        String find = "SELECT id_seccion FROM grados_secciones WHERE grado = ? AND letra = ?";
        try (PreparedStatement ps = c.prepareStatement(find)) {
            ps.setString(1, grado.trim());
            ps.setString(2, seccion.trim().toUpperCase(Locale.ROOT));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new IllegalArgumentException("No existe la sección " + grado + "-" + seccion + " en BD.");
    }

    private static void insertarInscripcionSiNoExiste(Connection c, int idEstudiante, int idSeccion, String anio) throws SQLException {
        String find = "SELECT id_inscripcion FROM inscripciones WHERE id_estudiante = ? AND anio_escolar = ?";
        try (PreparedStatement ps = c.prepareStatement(find)) {
            ps.setInt(1, idEstudiante);
            ps.setString(2, anio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String insert = "INSERT INTO inscripciones (id_estudiante, id_seccion, anio_escolar, estatus) VALUES (?, ?, ?, 'REGULAR')";
        try (PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setInt(1, idEstudiante);
            ps.setInt(2, idSeccion);
            ps.setString(3, anio);
            ps.executeUpdate();
        }
    }

    private static void validarCampos(String nombre, String apellido, String cedula, String seccion) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()
                || cedula == null || cedula.isBlank()) {
            throw new IllegalArgumentException("Nombre, apellido y cédula son obligatorios.");
        }
        if (!cedula.matches("\\d{7,9}")) {
            throw new IllegalArgumentException("La cédula debe contener solo números con 7 a 9 dígitos.");
        }
        if (!SECCIONES_VALIDAS.contains(seccion.trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Sección inválida.");
        }
    }

    private static String uppercase(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
