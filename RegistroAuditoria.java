package modelo;

public class RegistroAuditoria {

    private final String fechaHora;
    private final String usuario;
    private final String rol;
    private final String accion;
    private final String detalleAntes;
    private final String detalleDespues;

    public RegistroAuditoria(String fechaHora, String usuario, String rol, String accion,
            String detalleAntes, String detalleDespues) {
        this.fechaHora = fechaHora;
        this.usuario = usuario;
        this.rol = rol;
        this.accion = accion;
        this.detalleAntes = detalleAntes;
        this.detalleDespues = detalleDespues;
    }

    public String getFechaHora() { return fechaHora; }
    public String getUsuario() { return usuario; }
    public String getRol() { return rol; }
    public String getAccion() { return accion; }
    public String getDetalleAntes() { return detalleAntes; }
    public String getDetalleDespues() { return detalleDespues; }

    @Override
    public String toString() {
        return "[" + fechaHora + "] "
                + usuario + " (" + rol + ") -> " + accion
                + " | antes: " + detalleAntes
                + " | después: " + detalleDespues;
    }
}
