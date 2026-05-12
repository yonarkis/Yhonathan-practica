package controlador;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import modelo.AsistenciaRegistro;
import modelo.Docente;

public final class AsistenciaController {

    public static final List<String> ESTADOS = List.of("PRESENTE", "AUSENTE", "JUSTIFICADO");
    private static final List<AsistenciaRegistro> registros = new ArrayList<>();

    private AsistenciaController() {
    }

    public static void registrar(String cedulaEstudiante, String anioEstudio, String seccion, String estado) {
        registrar(cedulaEstudiante, anioEstudio, seccion, estado, LocalDate.now().toString());
    }

    public static void registrar(String cedulaEstudiante, String anioEstudio, String seccion, String estado, String fechaISO) {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, usuario != null && usuario.esDocente(),
                "Solo un docente puede registrar asistencia.");
        if (cedulaEstudiante == null || !cedulaEstudiante.matches("\\d{7,9}")) {
            throw new IllegalArgumentException("Cédula inválida para asistencia.");
        }
        if (!ESTADOS.contains(estado)) {
            throw new IllegalArgumentException("Estado de asistencia inválido.");
        }
        LocalDate fechaValidada;
        try {
            fechaValidada = LocalDate.parse(fechaISO);
        } catch (Exception ex) {
            throw new IllegalArgumentException("La fecha de asistencia debe tener formato YYYY-MM-DD.");
        }
        String fecha = fechaValidada.toString();
        registros.removeIf(r -> r.getCedulaEstudiante().equalsIgnoreCase(cedulaEstudiante)
                && r.getFecha().equals(fecha)
                && r.getAnioEstudio().equalsIgnoreCase(anioEstudio)
                && r.getSeccion().equalsIgnoreCase(seccion)
                && r.getDocenteUsuario().equalsIgnoreCase(usuario.getUsuario()));
        registros.add(new AsistenciaRegistro(cedulaEstudiante, fecha, anioEstudio, seccion, estado, usuario.getUsuario()));
        AuditoriaController.registrar("ASISTENCIA_" + estado,
                "Sin registro previo o reemplazado",
                cedulaEstudiante + " | " + anioEstudio + " | sección " + seccion + " | " + fecha);
    }

    public static List<AsistenciaRegistro> listarPorEstudiante(String cedulaEstudiante) {
        return registros.stream()
                .filter(r -> r.getCedulaEstudiante().equalsIgnoreCase(cedulaEstudiante))
                .collect(Collectors.toList());
    }

    public static List<AsistenciaRegistro> listarPorGrupo(String anioEstudio, String seccion, String docenteUsuario) {
        return registros.stream()
                .filter(r -> r.getAnioEstudio().equalsIgnoreCase(anioEstudio)
                        && r.getSeccion().equalsIgnoreCase(seccion)
                        && r.getDocenteUsuario().equalsIgnoreCase(docenteUsuario))
                .collect(Collectors.toList());
    }

    public static Map<String, long[]> resumenPorGrupo(String anioEstudio, String seccion, String docenteUsuario) {
        Map<String, long[]> resumen = new LinkedHashMap<>();
        for (AsistenciaRegistro registro : listarPorGrupo(anioEstudio, seccion, docenteUsuario)) {
            long[] contador = resumen.computeIfAbsent(registro.getCedulaEstudiante(), k -> new long[3]);
            switch (registro.getEstado()) {
                case "PRESENTE" -> contador[0]++;
                case "AUSENTE" -> contador[1]++;
                case "JUSTIFICADO" -> contador[2]++;
                default -> {
                }
            }
        }
        return resumen;
    }

    public static List<AsistenciaRegistro> listar() {
        return Collections.unmodifiableList(registros);
    }

    public static void limpiarMemoria() {
        registros.clear();
    }
}
