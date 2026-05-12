package modelo;

import java.time.LocalDateTime;

public class Nota {
    private final int idEstudiante;
    private final String materia;
    private final double nota;
    private final String anioEscolar;
    private final int lapso;
    private final int numeroEvaluacion;
    private final LocalDateTime fechaCarga;

    public Nota(int idEstudiante, String materia, double nota) {
        this(idEstudiante, materia, nota, "2024-2025", 1, 1, LocalDateTime.now());
    }

    public Nota(int idEstudiante, String materia, double nota, String anioEscolar,
            int lapso, int numeroEvaluacion, LocalDateTime fechaCarga) {
        this.idEstudiante = idEstudiante;
        this.materia = materia;
        this.nota = nota;
        this.anioEscolar = anioEscolar;
        this.lapso = lapso;
        this.numeroEvaluacion = numeroEvaluacion;
        this.fechaCarga = fechaCarga;
    }

    public int getIdEstudiante() { return idEstudiante; }
    public String getMateria() { return materia; }
    public double getNota() { return nota; }
    public String getAnioEscolar() { return anioEscolar; }
    public int getLapso() { return lapso; }
    public int getNumeroEvaluacion() { return numeroEvaluacion; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
}
