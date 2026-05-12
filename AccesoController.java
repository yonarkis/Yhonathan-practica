package controlador;

import modelo.Docente;

public final class AccesoController {

    private AccesoController() {
    }

    public static boolean puedeGestionarEstudiantes(Docente usuario) {
        return usuario != null && (usuario.esControlEstudios() || usuario.esDirector() || usuario.esAdmin());
    }

    public static boolean puedeModificarEstudiantes(Docente usuario) {
        return usuario != null && (usuario.esControlEstudios() || usuario.esAdmin());
    }

    public static boolean puedeGestionarNotas(Docente usuario) {
        return usuario != null && (usuario.esDocente() || usuario.esControlEstudios() || usuario.esAdmin());
    }

    public static boolean puedeCorregirNotas(Docente usuario) {
        return usuario != null && (usuario.esControlEstudios() || usuario.esDirector() || usuario.esAdmin());
    }

    public static boolean puedeVerBoletines(Docente usuario) {
        return usuario != null && (usuario.esControlEstudios() || usuario.esDirector() || usuario.esAdmin());
    }

    public static boolean puedeVerDashboard(Docente usuario) {
        return usuario != null && (usuario.esDirector() || usuario.esAdmin());
    }

    public static boolean puedeCerrarPeriodos(Docente usuario) {
        return usuario != null && (usuario.esDirector() || usuario.esAdmin());
    }

    public static boolean puedeVerAuditoria(Docente usuario) {
        return usuario != null && (usuario.esControlEstudios() || usuario.esDirector() || usuario.esAdmin());
    }

    public static void validar(Docente usuario, boolean permitido, String mensaje) {
        if (usuario == null || !permitido) {
            throw new IllegalStateException(mensaje);
        }
    }
}
