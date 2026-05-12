package modelo;

public class ObservacionDocente {

    private final String fechaHora;
    private final String docenteUsuario;
    private final String cedulaEstudiante;
    private final String tipo;
    private final String detalle;

    public ObservacionDocente(String fechaHora, String docenteUsuario, String cedulaEstudiante, String tipo, String detalle) {
        this.fechaHora = fechaHora;
        this.docenteUsuario = docenteUsuario;
        this.cedulaEstudiante = cedulaEstudiante;
        this.tipo = tipo;
        this.detalle = detalle;
    }

    public String getFechaHora() { return fechaHora; }
    public String getDocenteUsuario() { return docenteUsuario; }
    public String getCedulaEstudiante() { return cedulaEstudiante; }
    public String getTipo() { return tipo; }
    public String getDetalle() { return detalle; }
}
