package modelo;

public class ConfiguracionEvaluacion {
    private final String anioEscolar;
    private final String anioEstudio;
    private final String materia;
    private final int lapso;
    private int cantidadEvaluaciones;

    public ConfiguracionEvaluacion(String anioEscolar, String anioEstudio, String materia, int lapso, int cantidadEvaluaciones) {
        this.anioEscolar = anioEscolar;
        this.anioEstudio = anioEstudio;
        this.materia = materia;
        this.lapso = lapso;
        this.cantidadEvaluaciones = cantidadEvaluaciones;
    }

    public String getAnioEscolar() { return anioEscolar; }
    public String getAnioEstudio() { return anioEstudio; }
    public String getMateria() { return materia; }
    public int getLapso() { return lapso; }
    public int getCantidadEvaluaciones() { return cantidadEvaluaciones; }

    public void setCantidadEvaluaciones(int cantidadEvaluaciones) {
        this.cantidadEvaluaciones = cantidadEvaluaciones;
    }
}
