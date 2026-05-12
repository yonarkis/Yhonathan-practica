package Vista;

import modelo.Docente;

public class MenuPrincipal extends MenuDocente {
    public MenuPrincipal(String rol) {
        super(new Docente(0, rol, "", "Usuario", rol));
    }
}
