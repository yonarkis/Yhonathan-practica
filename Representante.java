package modelo;

public class Representante {
    private final String cedulaEstudiante;
    private final String cedulaMadre;
    private final String nombreMadre;
    private final String apellidoMadre;
    private final String cedulaRepresentante;
    private final String nombreRepresentante;
    private final String apellidoRepresentante;
    private final String parentescoRepresentante;

    public Representante(String cedulaEstudiante, String cedulaMadre, String nombreMadre,
            String apellidoMadre, String cedulaRepresentante, String nombreRepresentante,
            String apellidoRepresentante, String parentescoRepresentante) {
        this.cedulaEstudiante = cedulaEstudiante;
        this.cedulaMadre = cedulaMadre;
        this.nombreMadre = nombreMadre;
        this.apellidoMadre = apellidoMadre;
        this.cedulaRepresentante = cedulaRepresentante;
        this.nombreRepresentante = nombreRepresentante;
        this.apellidoRepresentante = apellidoRepresentante;
        this.parentescoRepresentante = parentescoRepresentante;
    }

    public String getCedulaEstudiante() { return cedulaEstudiante; }
    public String getCedulaMadre() { return cedulaMadre; }
    public String getNombreMadre() { return nombreMadre; }
    public String getApellidoMadre() { return apellidoMadre; }
    public String getCedulaRepresentante() { return cedulaRepresentante; }
    public String getNombreRepresentante() { return nombreRepresentante; }
    public String getApellidoRepresentante() { return apellidoRepresentante; }
    public String getParentescoRepresentante() { return parentescoRepresentante; }
}
