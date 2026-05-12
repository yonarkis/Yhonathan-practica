package controlador;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import modelo.Docente;
import modelo.Estudiante;
import modelo.InscripcionEscolar;
import modelo.Nota;

public class NotaController {

    public static final List<String> MATERIAS = List.of(
            "Castellano", "Inglés", "Matemáticas", "Historia", "Física", "Química");
    private static final int MAX_NOTAS_POR_MATERIA = 5;
    private static final List<Nota> notas = new ArrayList<>();

    public static void agregar(int idEstudiante, String materia, double nota) {
        validarMateriaYNota(materia, nota);
        long cantidad = notas.stream()
                .filter(n -> n.getIdEstudiante() == idEstudiante
                        && n.getMateria().equalsIgnoreCase(materia)
                        && n.getAnioEscolar().equalsIgnoreCase(AnioEscolarController.getAnioActivo()))
                .count();
        if (cantidad >= MAX_NOTAS_POR_MATERIA) {
            throw new IllegalArgumentException("Máximo de 5 notas por materia para este estudiante.");
        }
        notas.add(new Nota(idEstudiante, normalizarMateria(materia), nota,
                AnioEscolarController.getAnioActivo(), 1, (int) cantidad + 1, LocalDateTime.now()));
    }

    public static void guardarNotasPorCedula(String cedula, String materia, int lapso, List<Double> notasMateria) {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, AccesoController.puedeGestionarNotas(usuario),
                "Solo docente o control de estudios pueden cargar notas.");
        Estudiante estudiante = EstudianteController.buscarPorCedula(cedula);
        if (estudiante == null) {
            throw new IllegalArgumentException("No existe estudiante con esa cédula.");
        }
        InscripcionEscolar inscripcion = InscripcionController.buscarEnAnioActivo(cedula);
        if (inscripcion == null) {
            throw new IllegalArgumentException("El estudiante no tiene inscripción activa en el año escolar actual.");
        }

        String anio = AnioEscolarController.getAnioActivo();
        AnioEscolarController.validarEdicionPermitida(anio);
        if (usuario != null && usuario.esDocente() && lapso != LapsoController.getLapsoActivo()) {
            throw new IllegalStateException("Solo puede cargar notas en el lapso activo.");
        }
        LapsoController.validarEdicionPermitida(anio, lapso);
        if (!LapsoController.fechaDentroLapso(anio, lapso, LocalDate.now())) {
            throw new IllegalStateException("La fecha actual está fuera del rango permitido para el lapso " + lapso + ".");
        }
        validarMateria(materia);
        validarLoteNotas(notasMateria);

        int maxPermitidas = ConfiguracionEvaluacionController.obtenerCantidad(
                anio, inscripcion.getAnioEstudio(), materia, lapso);
        if (notasMateria.size() > maxPermitidas) {
            throw new IllegalArgumentException("Para esta materia/lapso solo se permiten " + maxPermitidas + " evaluaciones configuradas.");
        }

        List<Double> notasAnteriores = notas.stream()
                .filter(n -> n.getIdEstudiante() == estudiante.getId()
                        && n.getMateria().equalsIgnoreCase(materia)
                        && n.getAnioEscolar().equalsIgnoreCase(anio)
                        && n.getLapso() == lapso)
                .map(Nota::getNota)
                .collect(Collectors.toList());

        eliminarNotasMateriaLapso(estudiante.getId(), materia, anio, lapso);
        for (int i = 0; i < notasMateria.size(); i++) {
            double nota = notasMateria.get(i);
            validarMateriaYNota(materia, nota);
            notas.add(new Nota(estudiante.getId(), normalizarMateria(materia), nota,
                    anio, lapso, i + 1, LocalDateTime.now()));
        }

        AuditoriaController.registrar(
                notasAnteriores.isEmpty() ? "CARGA_NOTAS" : "ACTUALIZACION_NOTAS",
                "Estudiante " + cedula + " | " + materia + " | lapso " + lapso + " | notas " + formatearLista(notasAnteriores),
                "Estudiante " + cedula + " | " + materia + " | lapso " + lapso + " | notas " + formatearLista(notasMateria)
        );
    }

    public static void guardarCincoNotasPorCedula(String cedula, String materia, List<Double> notasMateria) {
        guardarNotasPorCedula(cedula, materia, 1, notasMateria);
    }

    public static void agregarPorCedula(String cedula, String materia, double nota) {
        guardarNotasPorCedula(cedula, materia, 1, List.of(nota));
    }

    public static double calcularPromedio(int idEstudiante) {
        List<Nota> delEstudiante = notas.stream().filter(n -> n.getIdEstudiante() == idEstudiante).toList();
        if (delEstudiante.isEmpty()) {
            return 0;
        }
        return delEstudiante.stream().mapToDouble(Nota::getNota).average().orElse(0);
    }

    public static double calcularPromedioMateria(int idEstudiante, String materia) {
        List<Nota> deMateria = notas.stream()
                .filter(n -> n.getIdEstudiante() == idEstudiante && n.getMateria().equalsIgnoreCase(materia))
                .toList();
        if (deMateria.isEmpty()) {
            return 0;
        }
        return deMateria.stream().mapToDouble(Nota::getNota).average().orElse(0);
    }

    public static boolean aprobadoMateria(int idEstudiante, String materia) {
        return calcularPromedioMateria(idEstudiante, materia) >= 10;
    }

    public static List<Double> obtenerNotasMateria(int idEstudiante, String materia) {
        return notas.stream()
                .filter(n -> n.getIdEstudiante() == idEstudiante && n.getMateria().equalsIgnoreCase(materia))
                .sorted(Comparator.comparingInt(Nota::getLapso).thenComparingInt(Nota::getNumeroEvaluacion))
                .map(Nota::getNota)
                .collect(Collectors.toList());
    }

    public static List<Nota> getNotas() {
        return Collections.unmodifiableList(notas);
    }

    public static List<Nota> notasDeEstudiante(int idEstudiante) {
        return notas.stream().filter(n -> n.getIdEstudiante() == idEstudiante).toList();
    }

    public static void limpiarMemoria() {
        notas.clear();
    }

    public static void eliminarNotasPorEstudiante(int idEstudiante) {
        List<Double> previas = notas.stream()
                .filter(n -> n.getIdEstudiante() == idEstudiante)
                .map(Nota::getNota)
                .collect(Collectors.toList());
        notas.removeIf(n -> n.getIdEstudiante() == idEstudiante);
        if (!previas.isEmpty()) {
            AuditoriaController.registrar("ELIMINACION_NOTAS_ESTUDIANTE",
                    "ID " + idEstudiante + " | notas " + formatearLista(previas),
                    "Notas eliminadas");
        }
    }

    private static void eliminarNotasMateriaLapso(int idEstudiante, String materia, String anioEscolar, int lapso) {
        notas.removeIf(n -> n.getIdEstudiante() == idEstudiante
                && n.getMateria().equalsIgnoreCase(materia)
                && n.getAnioEscolar().equalsIgnoreCase(anioEscolar)
                && n.getLapso() == lapso);
    }

    public static String generarBoletaPorCedula(String cedula) {
        Estudiante e = EstudianteController.buscarPorCedula(cedula);
        if (e == null) {
            throw new IllegalArgumentException("No existe estudiante con esa cédula.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("BOLETA DE CALIFICACIONES\n")
          .append("Año escolar: ").append(AnioEscolarController.getAnioActivo()).append("\n")
          .append("Estudiante: ").append(e.getNombreCompleto()).append("\n")
          .append("Cédula: ").append(e.getCedula()).append("\n");

        InscripcionEscolar inscripcion = InscripcionController.buscarEnAnioActivo(cedula);
        if (inscripcion != null) {
            sb.append("Año/Sección: ").append(inscripcion.getAnioEstudio()).append(" - ").append(inscripcion.getSeccion()).append("\n");
        }
        modelo.Representante representante = RepresentanteController.buscarPorCedulaEstudiante(cedula);
        if (representante != null) {
            sb.append("Madre o padre: ")
                    .append(representante.getNombreMadre()).append(" ").append(representante.getApellidoMadre())
                    .append(" | C.I.: ").append(representante.getCedulaMadre()).append("\n");
            if (representante.getNombreRepresentante() != null && !representante.getNombreRepresentante().isBlank()) {
                sb.append("Representante: ")
                        .append(representante.getNombreRepresentante()).append(" ").append(representante.getApellidoRepresentante())
                        .append(" | C.I.: ").append(representante.getCedulaRepresentante())
                        .append(" | Parentesco: ").append(representante.getParentescoRepresentante()).append("\n");
            }
        }
        sb.append("\n");

        for (String materia : MATERIAS) {
            List<Double> notasMateria = obtenerNotasMateria(e.getId(), materia);
            double promedio = calcularPromedioMateria(e.getId(), materia);
            String estado = promedio >= 10 ? "APROBADO" : "REPROBADO";
            sb.append(materia).append(" -> Notas: ").append(formatearCincoNotas(notasMateria))
              .append(" | Promedio: ").append(String.format("%.1f", promedio))
              .append(" (" + estado + ")\n");
        }

        sb.append(String.format("%nPromedio general: %.1f", calcularPromedio(e.getId())));
        return sb.toString();
    }

    public static Map<String, String> reportePromedioEstadoPorMateria(String cedula) {
        Estudiante e = EstudianteController.buscarPorCedula(cedula);
        if (e == null) {
            throw new IllegalArgumentException("No existe estudiante con esa cédula.");
        }
        Map<String, String> reporte = new LinkedHashMap<>();
        for (String materia : MATERIAS) {
            List<Double> notasMateria = obtenerNotasMateria(e.getId(), materia);
            if (notasMateria.isEmpty()) {
                continue;
            }
            double promedio = calcularPromedioMateria(e.getId(), materia);
            String estado = promedio >= 10 ? "APROBADO" : "REPROBADO";
            reporte.put(materia, "Notas: " + formatearCincoNotas(notasMateria)
                    + " | Promedio: " + String.format("%.1f", promedio)
                    + " | Estado: " + estado);
        }
        return reporte;
    }

    public static List<String> top10PorMateriaYSeccion(String materia, String seccion) {
        String materiaNorm = normalizarMateria(materia);
        String anio = AnioEscolarController.getAnioActivo();
        return InscripcionController.listarPorSeccion(anio, seccion).stream()
                .map(i -> EstudianteController.buscarPorCedula(i.getCedulaEstudiante()))
                .filter(e -> e != null)
                .map(e -> {
                    double promedio = calcularPromedioMateria(e.getId(), materiaNorm);
                    InscripcionEscolar i = InscripcionController.buscarEnAnioActivo(e.getCedula());
                    String sec = i == null ? "-" : i.getSeccion();
                    return String.format("%s | C.I.: %s | Sección: %s | Promedio %s: %.1f",
                            e.getNombreCompleto(), e.getCedula(), sec, materiaNorm, promedio);
                })
                .sorted(Comparator.comparingDouble((String linea) -> extraerPromedio(linea)).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    public static void guardarNotasPorGrupo(String materia, int lapso, Map<String, List<Double>> notasPorCedula) {
        if (notasPorCedula == null || notasPorCedula.isEmpty()) {
            throw new IllegalArgumentException("Debe cargar al menos un estudiante para guardar en grupo.");
        }
        for (Map.Entry<String, List<Double>> entry : notasPorCedula.entrySet()) {
            guardarNotasPorCedula(entry.getKey(), materia, lapso, entry.getValue());
        }
    }

    public static List<String> alertasAcademicasPorGrupo(String anioEstudio, String seccion, String materia) {
        List<String> alertas = new ArrayList<>();
        for (Estudiante estudiante : EstudianteController.listarPorAnioYSeccion(anioEstudio, seccion)) {
            List<Double> notasMateria = obtenerNotasMateria(estudiante.getId(), materia);
            if (notasMateria.isEmpty()) {
                alertas.add(estudiante.getNombreCompleto() + " | " + estudiante.getCedula()
                        + " | SIN NOTAS EN " + materia.toUpperCase(Locale.ROOT));
                continue;
            }
            double promedio = calcularPromedioMateria(estudiante.getId(), materia);
            if (promedio < 10) {
                alertas.add(estudiante.getNombreCompleto() + " | " + estudiante.getCedula()
                        + " | ALERTA BAJO RENDIMIENTO " + materia.toUpperCase(Locale.ROOT)
                        + " | promedio " + String.format("%.1f", promedio));
            }
        }
        return alertas;
    }

    private static String formatearCincoNotas(List<Double> notasMateria) {
        List<String> notasTxt = new ArrayList<>();
        for (int i = 0; i < MAX_NOTAS_POR_MATERIA; i++) {
            if (i < notasMateria.size()) {
                notasTxt.add(String.format("%.1f", notasMateria.get(i)));
            } else {
                notasTxt.add("-");
            }
        }
        return String.join(", ", notasTxt);
    }

    private static void validarMateriaYNota(String materia, double nota) {
        validarMateria(materia);
        if (nota < 0.1 || nota > 20) {
            throw new IllegalArgumentException("La nota debe estar entre 0.1 y 20.");
        }
    }

    private static void validarMateria(String materia) {
        if (materia == null || !MATERIAS.stream().map(String::toLowerCase).toList().contains(materia.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Materia inválida.");
        }
    }

    private static void validarLoteNotas(List<Double> notasMateria) {
        if (notasMateria == null || notasMateria.isEmpty() || notasMateria.size() > MAX_NOTAS_POR_MATERIA) {
            throw new IllegalArgumentException("Debe ingresar entre 1 y 5 notas.");
        }
        notasMateria.forEach(n -> {
            if (n == null || n < 0.1 || n > 20) {
                throw new IllegalArgumentException("Cada nota debe estar entre 0.1 y 20.");
            }
        });
    }

    private static String normalizarMateria(String materia) {
        return MATERIAS.stream()
                .filter(m -> m.equalsIgnoreCase(materia.trim()))
                .findFirst()
                .orElse(materia.trim());
    }

    private static double extraerPromedio(String linea) {
        int idx = linea.lastIndexOf(':');
        return Double.parseDouble(linea.substring(idx + 1).trim());
    }

    private static String formatearLista(List<Double> notasMateria) {
        if (notasMateria == null || notasMateria.isEmpty()) {
            return "[]";
        }
        return notasMateria.stream()
                .map(n -> String.format("%.1f", n))
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
