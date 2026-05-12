package modelo;

public class Estudiante {
    private final int id;
    private String nombre;
    private String segundoNombre;
    private String apellido;
    private String segundoApellido;
    private final String cedula;
    private String fechaNacimiento;
    private String grado;
    private String seccion;

    public Estudiante(int id, String cedula, String nombre, String apellido) {
        this(id, nombre, "", apellido, "", cedula, "", "", "A");
    }

    public Estudiante(int id, String nombre, String segundoNombre, String apellido, String segundoApellido,
            String cedula, String fechaNacimiento, String grado, String seccion) {
        this.id = id;
        this.nombre = nombre;
        this.segundoNombre = segundoNombre;
        this.apellido = apellido;
        this.segundoApellido = segundoApellido;
        this.cedula = cedula;
        this.fechaNacimiento = fechaNacimiento;
        this.grado = grado;
        this.seccion = seccion;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSegundoNombre() { return segundoNombre; }
    public String getApellido() { return apellido; }
    public String getSegundoApellido() { return segundoApellido; }
    public String getCedula() { return cedula; }
    public String getFechaNacimiento() { return fechaNacimiento; }
    public String getGrado() { return grado; }
    public String getSeccion() { return seccion; }

    public String getNombreCompleto() {
        return String.format("%s %s %s %s", nombre, segundoNombre, apellido, segundoApellido).trim().replaceAll("\\s+", " ");
    }

    public void actualizar(String nombre, String segundoNombre, String apellido, String segundoApellido,
            String fechaNacimiento, String grado, String seccion) {
        this.nombre = nombre;
        this.segundoNombre = segundoNombre;
        this.apellido = apellido;
        this.segundoApellido = segundoApellido;
        this.fechaNacimiento = fechaNacimiento;
        this.grado = grado;
        this.seccion = seccion;
    }
}
