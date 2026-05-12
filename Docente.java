package modelo;

public class Docente {

    public static final String ROL_DOCENTE = "docente";
    public static final String ROL_CONTROL_ESTUDIOS = "control_estudios";
    public static final String ROL_DIRECTOR = "director";
    public static final String ROL_ADMIN = "admin";

    private final int id;
    private final String usuario;
    private final String clave;
    private final String nombre;
    private final String rol;

    public Docente(int id, String usuario, String clave, String nombre, String rol) {
        this.id = id;
        this.rol = rol;
        this.usuario = usuario;
        this.clave = clave;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getClave() { return clave; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }

    public boolean esDocente() {
        return ROL_DOCENTE.equalsIgnoreCase(rol);
    }

    public boolean esControlEstudios() {
        return ROL_CONTROL_ESTUDIOS.equalsIgnoreCase(rol);
    }

    public boolean esDirector() {
        return ROL_DIRECTOR.equalsIgnoreCase(rol);
    }

    public boolean esAdmin() {
        return ROL_ADMIN.equalsIgnoreCase(rol);
    }
}
