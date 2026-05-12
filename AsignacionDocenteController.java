package controlador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.LocalTime;
import modelo.AsignacionDocente;
import modelo.Docente;

public final class AsignacionDocenteController {

    private static final List<AsignacionDocente> asignaciones = new ArrayList<>();
    private static final Set<String> asignacionKeys = new LinkedHashSet<>();
    private static final Map<String, Integer> profesoresGuia = new LinkedHashMap<>();
    private static final Map<Integer, Set<String>> materiasPorDocente = new LinkedHashMap<>();
    private static final Map<Integer, Set<String>> materiasPermitidasPorDocente = new LinkedHashMap<>();
    private static final int MAX_MATERIAS_POR_DOCENTE = 4;
    private static final int MAX_CURSOS_POR_DOCENTE = 12;

    static {
        cargarPerfilesDocentes();
    }

    private AsignacionDocenteController() {
    }

    public static List<AsignacionDocente> listarPorDocente(int idDocente) {
        String anio = AnioEscolarController.getAnioActivo();
        return asignaciones.stream()
                .filter(a -> a.getIdDocente() == idDocente && a.getAnioEscolar().equalsIgnoreCase(anio))
                .collect(Collectors.toList());
    }

    public static List<AsignacionDocente> listar() {
        return Collections.unmodifiableList(asignaciones);
    }

    public static void limpiarAsignacionesMemoria() {
        asignaciones.clear();
        asignacionKeys.clear();
        profesoresGuia.clear();
        materiasPorDocente.clear();
    }

    public static void reinicializarCoberturaDemo() {
        limpiarAsignacionesMemoria();
        cargarPerfilesDocentes();
    }

    public static List<Docente> docentesAsignables() {
        return DocenteController.listar().stream()
                .filter(Docente::esDocente)
                .collect(Collectors.toList());
    }

    public static List<String> materiasParaAnio(String anioEstudio) {
        List<String> materias = new ArrayList<>(List.of("Castellano", "Inglés", "Matemáticas", "Historia"));
        if (anioEstudio != null && (anioEstudio.startsWith("Tercer") || anioEstudio.startsWith("Cuarto") || anioEstudio.startsWith("Quinto"))) {
            materias.add("Física");
            materias.add("Química");
        }
        return materias;
    }

    public static List<Docente> docentesPorMateriaDisponible(String materia, String anioEstudio, String seccion) {
        return docentesAsignables().stream()
                .filter(d -> puedeImpartirMateria(d.getId(), materia))
                .filter(d -> tieneDisponibilidad(d.getId()) || yaTieneAsignada(d.getId(), anioEstudio, seccion, materia))
                .collect(Collectors.toList());
    }

    public static int cargaActualCursos(int idDocente) {
        String anioEscolar = AnioEscolarController.getAnioActivo();
        return (int) asignaciones.stream()
                .filter(a -> a.getIdDocente() == idDocente && a.getAnioEscolar().equalsIgnoreCase(anioEscolar))
                .count();
    }

    public static int capacidadMaximaCursos() {
        return MAX_CURSOS_POR_DOCENTE;
    }

    public static Set<String> materiasPermitidas(int idDocente) {
        return Collections.unmodifiableSet(materiasPermitidasPorDocente.getOrDefault(idDocente, Set.of()));
    }

    public static void asignarMateria(int idDocente, String anioEstudio, String seccion, String materia,
            String diaSemana, String horaInicio, String horaFin) {
        Docente docente = DocenteController.buscarPorId(idDocente);
        if (docente == null || !docente.esDocente()) {
            throw new IllegalArgumentException("Debe seleccionar un docente válido.");
        }
        if (!InscripcionController.ANIOS_ESTUDIO_VALIDOS.contains(anioEstudio)) {
            throw new IllegalArgumentException("Año de estudio inválido.");
        }
        if (!InscripcionController.SECCIONES_VALIDAS.contains(seccion)) {
            throw new IllegalArgumentException("Sección inválida.");
        }
        if (!materiasParaAnio(anioEstudio).contains(materia)) {
            throw new IllegalArgumentException("La materia " + materia + " no corresponde para " + anioEstudio + ".");
        }
        validarHorario(diaSemana, horaInicio, horaFin);

        Set<String> materias = materiasPorDocente.computeIfAbsent(idDocente, key -> new LinkedHashSet<>());
        Set<String> permitidas = materiasPermitidasPorDocente.getOrDefault(idDocente, Set.of());
        if (!permitidas.isEmpty() && !permitidas.contains(materia)) {
            throw new IllegalArgumentException("El docente seleccionado no está perfilado para " + materia + ".");
        }
        if (materias.size() >= MAX_MATERIAS_POR_DOCENTE && !materias.contains(materia)) {
            throw new IllegalArgumentException("Cada docente puede cubrir máximo 4 materias distintas.");
        }

        String anioEscolar = AnioEscolarController.getAnioActivo();
        String key = key(idDocente, anioEscolar, anioEstudio, seccion, materia);
        if (asignacionKeys.contains(key)) {
            return;
        }
        if (!tieneDisponibilidad(idDocente)) {
            throw new IllegalArgumentException("El docente ya no tiene disponibilidad de cursos para asignar.");
        }

        reemplazarAsignacionMateria(anioEscolar, anioEstudio, seccion, materia);
        asignaciones.add(new AsignacionDocente(idDocente, anioEscolar, anioEstudio, seccion, materia, diaSemana, horaInicio, horaFin));
        asignacionKeys.add(key);
        materias.add(materia);
        AuditoriaController.registrar("ASIGNACION_DOCENTE",
                "-",
                docente.getUsuario() + " => " + anioEstudio + "-" + seccion + " " + materia + " | " + diaSemana + " " + horaInicio + "-" + horaFin);
    }

    public static void asignarProfesorGuia(String anioEstudio, String seccion, int idDocente) {
        Docente docente = DocenteController.buscarPorId(idDocente);
        if (docente == null || !docente.esDocente()) {
            throw new IllegalArgumentException("Docente guía inválido.");
        }
        String key = anioEstudio + "#" + seccion;
        profesoresGuia.put(key, idDocente);
        AuditoriaController.registrar("ASIGNACION_GUIA",
                "-",
                anioEstudio + "-" + seccion + " => " + docente.getUsuario());
    }

    public static Docente buscarProfesorGuia(String anioEstudio, String seccion) {
        Integer id = profesoresGuia.get(anioEstudio + "#" + seccion);
        return id == null ? null : DocenteController.buscarPorId(id);
    }

    public static List<String> seccionesGuiadasPorDocente(int idDocente) {
        return profesoresGuia.entrySet().stream()
                .filter(e -> e.getValue() == idDocente)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
    }

    public static List<AsignacionDocente> listarPorAnioSeccion(String anioEstudio, String seccion) {
        String anio = AnioEscolarController.getAnioActivo();
        return asignaciones.stream()
                .filter(a -> a.getAnioEscolar().equalsIgnoreCase(anio)
                        && a.getAnioEstudio().equalsIgnoreCase(anioEstudio)
                        && a.getSeccion().equalsIgnoreCase(seccion))
                .collect(Collectors.toList());
    }

    private static boolean puedeImpartirMateria(int idDocente, String materia) {
        Set<String> permitidas = materiasPermitidasPorDocente.getOrDefault(idDocente, Set.of());
        return permitidas.isEmpty() || permitidas.contains(materia);
    }

    private static boolean tieneDisponibilidad(int idDocente) {
        return cargaActualCursos(idDocente) < MAX_CURSOS_POR_DOCENTE;
    }

    private static boolean yaTieneAsignada(int idDocente, String anioEstudio, String seccion, String materia) {
        String anioEscolar = AnioEscolarController.getAnioActivo();
        return asignacionKeys.contains(key(idDocente, anioEscolar, anioEstudio, seccion, materia));
    }

    private static void reemplazarAsignacionMateria(String anioEscolar, String anioEstudio, String seccion, String materia) {
        List<AsignacionDocente> previas = asignaciones.stream()
                .filter(a -> a.getAnioEscolar().equalsIgnoreCase(anioEscolar)
                        && a.getAnioEstudio().equalsIgnoreCase(anioEstudio)
                        && a.getSeccion().equalsIgnoreCase(seccion)
                        && a.getMateria().equalsIgnoreCase(materia))
                .toList();
        if (previas.isEmpty()) {
            return;
        }
        for (AsignacionDocente previa : previas) {
            asignaciones.remove(previa);
            asignacionKeys.remove(key(previa.getIdDocente(), previa.getAnioEscolar(), previa.getAnioEstudio(), previa.getSeccion(), previa.getMateria()));
            Set<String> materias = materiasPorDocente.getOrDefault(previa.getIdDocente(), new LinkedHashSet<>());
            boolean aunTieneMateria = asignaciones.stream()
                    .anyMatch(a -> a.getIdDocente() == previa.getIdDocente()
                    && a.getAnioEscolar().equalsIgnoreCase(anioEscolar)
                    && a.getMateria().equalsIgnoreCase(previa.getMateria()));
            if (!aunTieneMateria) {
                materias.remove(previa.getMateria());
            }
            materiasPorDocente.put(previa.getIdDocente(), materias);
        }
    }

    private static void cargarPerfilesDocentes() {
        materiasPermitidasPorDocente.put(1, Set.of("Matemáticas", "Castellano", "Inglés"));
        materiasPermitidasPorDocente.put(5, Set.of("Matemáticas", "Física", "Química"));
        materiasPermitidasPorDocente.put(6, Set.of("Castellano", "Inglés", "Historia"));
        materiasPermitidasPorDocente.put(7, Set.of("Historia", "Castellano", "Inglés"));
        materiasPermitidasPorDocente.put(8, Set.of("Física", "Química", "Matemáticas"));
        materiasPermitidasPorDocente.put(9, Set.of("Matemáticas", "Historia", "Inglés"));
        materiasPermitidasPorDocente.put(10, Set.of("Castellano", "Inglés", "Historia"));
        materiasPermitidasPorDocente.put(11, Set.of("Física", "Matemáticas", "Historia"));
        materiasPermitidasPorDocente.put(12, Set.of("Química", "Física", "Historia"));
    }

    private static String key(int idDocente, String anioEscolar, String anioEstudio, String seccion, String materia) {
        return idDocente + "|" + anioEscolar + "|" + anioEstudio + "|" + seccion + "|" + materia;
    }

    private static void validarHorario(String diaSemana, String horaInicio, String horaFin) {
        Set<String> dias = Set.of("Lunes", "Martes", "Miércoles", "Jueves", "Viernes");
        if (diaSemana == null || !dias.contains(diaSemana)) {
            throw new IllegalArgumentException("Debe seleccionar un día válido de lunes a viernes.");
        }
        try {
            LocalTime inicio = LocalTime.parse(horaInicio);
            LocalTime fin = LocalTime.parse(horaFin);
            LocalTime minimo = LocalTime.of(7, 30);
            LocalTime maximo = LocalTime.of(16, 0);
            if (inicio.isBefore(minimo) || fin.isAfter(maximo)) {
                throw new IllegalArgumentException("El horario permitido es de 07:30 a 16:00.");
            }
            long minutos = Duration.between(inicio, fin).toMinutes();
            if (minutos < 90 || minutos > 120) {
                throw new IllegalArgumentException("Cada bloque de materia debe durar entre 1h30 y 2h.");
            }
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException) {
                throw ex;
            }
            throw new IllegalArgumentException("Formato de horario inválido. Use HH:MM (24h).");
        }
    }
}
