package Vista;

import controlador.DocenteController;
import controlador.DatabaseConnection;
import controlador.SessionContext;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import modelo.Docente;

public class LoginDocente extends JFrame {

    private final JTextField txtUsuario = new JTextField(18);
    private final JPasswordField txtClave = new JPasswordField(18);
    private final JLabel lblDbStatus = new JLabel();

    public LoginDocente() {
        setTitle("Sistema de Notas - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        ((AbstractDocument) txtUsuario.getDocument()).setDocumentFilter(new AlphanumericDocumentFilter(40));

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.add(UiStyles.topBar("Sistema Automatizado de Notas"), BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        UiStyles.card(card);
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        formulario.add(UiStyles.titleLabel("Bienvenido"), gbc);

        gbc.gridy = 1;
        formulario.add(UiStyles.subtitleLabel("Acceso al sistema académico"), gbc);

        gbc.gridy = 2;
        formulario.add(UiStyles.subtitleLabel("Estado de conexión a base de datos"), gbc);

        gbc.gridy = 3;
        actualizarEstadoBd();
        formulario.add(lblDbStatus, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 4;
        formulario.add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(txtUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        formulario.add(new JLabel("Clave:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formulario.add(txtClave, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        javax.swing.JButton btnLogin = UiStyles.primaryButton("Iniciar sesión");
        btnLogin.addActionListener(e -> login());
        formulario.add(btnLogin, gbc);

        card.add(formulario);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void actualizarEstadoBd() {
        boolean ok = DatabaseConnection.canConnect();
        lblDbStatus.setText(ok ? "BD: conectada ✅" : "BD: sin conexión ⚠");
        lblDbStatus.setForeground(ok ? new java.awt.Color(0, 130, 60) : new java.awt.Color(170, 110, 0));
    }

    private void login() {
        actualizarEstadoBd();
        if (!DatabaseConnection.canConnect()) {
            System.out.println("DB sin conexión: " + DatabaseConnection.getLastError());
        }
        String usuario = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword()).trim();

        Docente docente = DocenteController.login(usuario, clave);
        if (docente == null) {
            JOptionPane.showMessageDialog(this, "Usuario o clave incorrectos");
            return;
        }
        SessionContext.setUsuarioActual(docente);
        JOptionPane.showMessageDialog(this, "Bienvenido " + docente.getNombre() + " (" + docente.getRol() + ")");
        new MenuDocente(docente).setVisible(true);
        dispose();
    }
}
