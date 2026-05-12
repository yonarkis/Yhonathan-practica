package modelo;

public class AsistenciaRegistro {

    private final String cedulaEstudiante;
    private final String fecha;
    private final String anioEstudio;
    private final String seccion;
    private final String estado;
    private final String docenteUsuario;

    public AsistenciaRegistro(String cedulaEstudiante, String fecha, String anioEstudio,
            String seccion, String estado, String docenteUsuario) {
        this.cedulaEstudiante = cedulaEstudiante;
        this.fecha = fecha;
        this.anioEstudio = anioEstudio;
        this.seccion = seccion;
        this.estado = estado;
        this.docenteUsuario = docenteUsuario;
    }

    public String getCedulaEstudiante() { return cedulaEstudiante; }
    public String getFecha() { return fecha; }
    public String getAnioEstudio() { return anioEstudio; }
    public String getSeccion() { return seccion; }
    public String getEstado() { return estado; }
    public String getDocenteUsuario() { return docenteUsuario; }
}
