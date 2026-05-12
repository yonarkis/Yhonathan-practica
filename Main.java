package sistemacalificaciones;

import Vista.LoginDocente;

public class Main {

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginDocente().setVisible(true);
            }
        });

    }

}
