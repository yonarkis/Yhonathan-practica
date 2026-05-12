package modelo;

public class InscripcionEscolar {
    private final String cedulaEstudiante;
    private final String anioEscolar;
    private String anioEstudio;
    private String seccion;
    private String estatus;

    public InscripcionEscolar(String cedulaEstudiante, String anioEscolar, String anioEstudio, String seccion, String estatus) {
        this.cedulaEstudiante = cedulaEstudiante;
        this.anioEscolar = anioEscolar;
        this.anioEstudio = anioEstudio;
        this.seccion = seccion;
        this.estatus = estatus;
    }

    public String getCedulaEstudiante() { return cedulaEstudiante; }
    public String getAnioEscolar() { return anioEscolar; }
    public String getAnioEstudio() { return anioEstudio; }
    public String getSeccion() { return seccion; }
    public String getEstatus() { return estatus; }

    public void actualizar(String anioEstudio, String seccion, String estatus) {
        this.anioEstudio = anioEstudio;
        this.seccion = seccion;
        this.estatus = estatus;
    }
}
