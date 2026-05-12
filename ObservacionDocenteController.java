package controlador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import modelo.Docente;
import modelo.ObservacionDocente;

public final class ObservacionDocenteController {

    public static final List<String> TIPOS = List.of("ACADEMICA", "CONDUCTA", "ASISTENCIA", "SEGUIMIENTO", "RASGO_PERSONALIDAD");
    private static final List<ObservacionDocente> observaciones = new ArrayList<>();
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ObservacionDocenteController() {
    }

    public static void registrar(String cedulaEstudiante, String tipo, String detalle) {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, usuario != null && usuario.esDocente(),
                "Solo un docente puede registrar observaciones.");
        if (cedulaEstudiante == null || !cedulaEstudiante.matches("\\d{7,9}")) {
            throw new IllegalArgumentException("Cédula inválida para observación.");
        }
        if (tipo == null || !TIPOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de observación inválido.");
        }
        if (detalle == null || detalle.isBlank()) {
            throw new IllegalArgumentException("Debe escribir el detalle de la observación.");
        }
        observaciones.add(new ObservacionDocente(
                LocalDateTime.now().format(FORMATO),
                usuario.getUsuario(),
                cedulaEstudiante,
                tipo,
                detalle.trim()));
        AuditoriaController.registrar("OBSERVACION_" + tipo,
                "-",
                cedulaEstudiante + " | " + detalle.trim());
    }

    public static List<ObservacionDocente> listarPorDocente(String docenteUsuario) {
        return observaciones.stream()
                .filter(o -> o.getDocenteUsuario().equalsIgnoreCase(docenteUsuario))
                .collect(Collectors.toList());
    }

    public static List<ObservacionDocente> listarPorEstudiante(String cedulaEstudiante) {
        return observaciones.stream()
                .filter(o -> o.getCedulaEstudiante().equalsIgnoreCase(cedulaEstudiante))
                .collect(Collectors.toList());
    }

    public static List<ObservacionDocente> listar() {
        return Collections.unmodifiableList(observaciones);
    }

    public static void limpiarMemoria() {
        observaciones.clear();
    }
}
