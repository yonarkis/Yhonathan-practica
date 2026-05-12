package controlador;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LapsoController {

    private static final Set<String> lapsosCerrados = new HashSet<>();
    private static final Map<String, LocalDate[]> rangosLapso = new LinkedHashMap<>();
    private static int lapsoActivo = 1;

    private LapsoController() {
    }

    public static void cerrarLapso(String anioEscolar, int lapso) {
        validar(anioEscolar, lapso);
        lapsosCerrados.add(clave(anioEscolar, lapso));
        AuditoriaController.registrar("CIERRE_LAPSO",
                "Lapso abierto " + lapso + " en " + anioEscolar,
                "Lapso cerrado " + lapso + " en " + anioEscolar);
    }

    public static boolean estaCerrado(String anioEscolar, int lapso) {
        validar(anioEscolar, lapso);
        return lapsosCerrados.contains(clave(anioEscolar, lapso));
    }

    public static int getLapsoActivo() {
        return lapsoActivo;
    }

    public static void setLapsoActivo(int lapso) {
        if (lapso < 1 || lapso > 3) {
            throw new IllegalArgumentException("Lapso activo inválido.");
        }
        lapsoActivo = lapso;
    }

    public static void configurarRangoLapso(String anioEscolar, int lapso, String fechaInicio, String fechaFin) {
        validar(anioEscolar, lapso);
        LocalDate inicio;
        LocalDate fin;
        try {
            inicio = LocalDate.parse(fechaInicio);
            fin = LocalDate.parse(fechaFin);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Fechas de lapso inválidas. Use YYYY-MM-DD.");
        }
        if (fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha fin del lapso no puede ser menor que la fecha inicio.");
        }
        rangosLapso.put(clave(anioEscolar, lapso), new LocalDate[]{inicio, fin});
    }

    public static boolean fechaDentroLapso(String anioEscolar, int lapso, LocalDate fecha) {
        LocalDate[] rango = rangosLapso.get(clave(anioEscolar, lapso));
        if (rango == null) {
            return true;
        }
        return !(fecha.isBefore(rango[0]) || fecha.isAfter(rango[1]));
    }

    public static void validarEdicionPermitida(String anioEscolar, int lapso) {
        if (estaCerrado(anioEscolar, lapso)) {
            throw new IllegalStateException("El lapso " + lapso + " del año " + anioEscolar + " está cerrado.");
        }
    }

    private static String clave(String anioEscolar, int lapso) {
        return anioEscolar + "#" + lapso;
    }

    private static void validar(String anioEscolar, int lapso) {
        if (anioEscolar == null || !anioEscolar.matches("\\d{4}-\\d{4}")) {
            throw new IllegalArgumentException("Año escolar inválido.");
        }
        if (lapso < 1 || lapso > 3) {
            throw new IllegalArgumentException("Lapso inválido.");
        }
    }
}
