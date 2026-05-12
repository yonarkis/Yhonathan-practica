package controlador;

import java.util.ArrayList;
import java.util.List;
import modelo.ConfiguracionEvaluacion;

public class ConfiguracionEvaluacionController {

    private static final int MIN_EVALUACIONES = 3;
    private static final int MAX_EVALUACIONES = 5;
    private static final List<ConfiguracionEvaluacion> configuraciones = new ArrayList<>();

    public static void configurar(String anioEscolar, String anioEstudio, String materia, int lapso, int cantidadEvaluaciones) {
        validar(cantidadEvaluaciones, lapso);
        ConfiguracionEvaluacion existente = buscar(anioEscolar, anioEstudio, materia, lapso);
        if (existente == null) {
            configuraciones.add(new ConfiguracionEvaluacion(anioEscolar, anioEstudio, materia, lapso, cantidadEvaluaciones));
            return;
        }
        existente.setCantidadEvaluaciones(cantidadEvaluaciones);
    }

    public static int obtenerCantidad(String anioEscolar, String anioEstudio, String materia, int lapso) {
        ConfiguracionEvaluacion config = buscar(anioEscolar, anioEstudio, materia, lapso);
        return config == null ? MAX_EVALUACIONES : config.getCantidadEvaluaciones();
    }

    private static ConfiguracionEvaluacion buscar(String anioEscolar, String anioEstudio, String materia, int lapso) {
        return configuraciones.stream()
                .filter(c -> c.getAnioEscolar().equalsIgnoreCase(anioEscolar)
                        && c.getAnioEstudio().equalsIgnoreCase(anioEstudio)
                        && c.getMateria().equalsIgnoreCase(materia)
                        && c.getLapso() == lapso)
                .findFirst()
                .orElse(null);
    }

    private static void validar(int cantidadEvaluaciones, int lapso) {
        if (lapso < 1 || lapso > 3) {
            throw new IllegalArgumentException("Lapso inválido. Use 1, 2 o 3.");
        }
        if (cantidadEvaluaciones < MIN_EVALUACIONES || cantidadEvaluaciones > MAX_EVALUACIONES) {
            throw new IllegalArgumentException("La cantidad de evaluaciones debe estar entre 3 y 5.");
        }
    }
}
