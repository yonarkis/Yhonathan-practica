package controlador;

import java.util.HashSet;
import java.util.Set;

public class AnioEscolarController {

    private static final Set<String> aniosCerrados = new HashSet<>();
    private static String anioActivo = "2024-2025";

    public static String getAnioActivo() {
        return anioActivo;
    }

    public static void setAnioActivo(String anioEscolar) {
        validarAnio(anioEscolar);
        anioActivo = anioEscolar;
    }

    public static void cerrarAnio(String anioEscolar) {
        validarAnio(anioEscolar);
        if (aniosCerrados.contains(anioEscolar)) {
            throw new IllegalStateException("El año escolar " + anioEscolar + " ya está cerrado.");
        }
        aniosCerrados.add(anioEscolar);
        AuditoriaController.registrar("CIERRE_ANIO",
                "Año abierto " + anioEscolar,
                "Año cerrado " + anioEscolar);
    }

    public static boolean estaCerrado(String anioEscolar) {
        validarAnio(anioEscolar);
        return aniosCerrados.contains(anioEscolar);
    }

    public static void validarEdicionPermitida(String anioEscolar) {
        if (estaCerrado(anioEscolar)) {
            throw new IllegalStateException("El año escolar " + anioEscolar + " está cerrado. Solo se permite consulta.");
        }
    }

    private static void validarAnio(String anioEscolar) {
        if (anioEscolar == null || !anioEscolar.matches("\\d{4}-\\d{4}")) {
            throw new IllegalArgumentException("Año escolar inválido. Formato esperado: 2024-2025.");
        }
    }
}
