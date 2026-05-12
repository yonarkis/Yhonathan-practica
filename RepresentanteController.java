package controlador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Representante;

public class RepresentanteController {

    private static final List<Representante> representantes = new ArrayList<>();

    public static void registrar(String cedulaEstudiante, String cedulaMadre, String nombreMadre,
            String apellidoMadre, String cedulaRepresentante, String nombreRepresentante,
            String apellidoRepresentante, String parentescoRepresentante) {
        validar(cedulaEstudiante, cedulaMadre, nombreMadre, apellidoMadre);
        representantes.removeIf(r -> r.getCedulaEstudiante().equalsIgnoreCase(cedulaEstudiante.trim()));
        representantes.add(new Representante(cedulaEstudiante.trim(), cedulaMadre.trim(),
                normalize(nombreMadre), normalize(apellidoMadre),
                cedulaRepresentante == null ? "" : cedulaRepresentante.trim(),
                normalize(nombreRepresentante), normalize(apellidoRepresentante),
                normalize(parentescoRepresentante)));
    }

    public static Representante buscarPorCedulaEstudiante(String cedulaEstudiante) {
        return representantes.stream()
                .filter(r -> r.getCedulaEstudiante().equalsIgnoreCase(cedulaEstudiante.trim()))
                .findFirst()
                .orElse(null);
    }

    public static List<Representante> listar() {
        return Collections.unmodifiableList(representantes);
    }

    private static void validar(String cedulaEstudiante, String cedulaMadre, String nombreMadre, String apellidoMadre) {
        if (cedulaEstudiante == null || cedulaEstudiante.isBlank()) {
            throw new IllegalArgumentException("Cédula del estudiante obligatoria.");
        }
        if (cedulaMadre == null || !cedulaMadre.matches("\\d{7,9}")) {
            throw new IllegalArgumentException("Cédula de la madre o padre obligatoria (solo números, 7 a 9 dígitos).");
        }
        if (nombreMadre == null || nombreMadre.isBlank()) {
            throw new IllegalArgumentException("Nombre de la madre o padre obligatorio.");
        }
        if (apellidoMadre == null || apellidoMadre.isBlank()) {
            throw new IllegalArgumentException("Apellido de la madre o padre obligatorio.");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
