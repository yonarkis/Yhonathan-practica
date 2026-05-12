package modelo;

public class AsignacionDocente {

    private final int idDocente;
    private final String anioEscolar;
    private final String anioEstudio;
    private final String seccion;
    private final String materia;
    private final String diaSemana;
    private final String horaInicio;
    private final String horaFin;

    public AsignacionDocente(int idDocente, String anioEscolar, String anioEstudio, String seccion, String materia,
            String diaSemana, String horaInicio, String horaFin) {
        this.idDocente = idDocente;
        this.anioEscolar = anioEscolar;
        this.anioEstudio = anioEstudio;
        this.seccion = seccion;
        this.materia = materia;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public int getIdDocente() { return idDocente; }
    public String getAnioEscolar() { return anioEscolar; }
    public String getAnioEstudio() { return anioEstudio; }
    public String getSeccion() { return seccion; }
    public String getMateria() { return materia; }
    public String getDiaSemana() { return diaSemana; }
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }

    public String getEtiqueta() {
        return anioEstudio + " - Sección " + seccion + " - " + materia + " (" + diaSemana + " " + horaInicio + "-" + horaFin + ")";
    }
}
