package controlador;

import modelo.Docente;

public final class SessionContext {

    private static Docente usuarioActual;

    private SessionContext() {
    }

    public static void setUsuarioActual(Docente usuario) {
        usuarioActual = usuario;
    }

    public static Docente getUsuarioActual() {
        return usuarioActual;
    }

    public static void clear() {
        usuarioActual = null;
    }
}
