package Vista;

import controlador.AccesoController;
import controlador.AnioEscolarController;
import controlador.EstudianteController;
import controlador.RepresentanteController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import modelo.Docente;
import modelo.Estudiante;

public class RegistrarEstudiantes extends JFrame {

    private final JTextField txtNombre = new JTextField(16);
    private final JTextField txtSegundoNombre = new JTextField(16);
    private final JTextField txtApellido = new JTextField(16);
    private final JTextField txtSegundoApellido = new JTextField(16);
    private final JTextField txtCedula = new JTextField(16);

    private final JTextField txtCedulaMadre = new JTextField(16);
    private final JTextField txtNombreMadre = new JTextField(16);
    private final JTextField txtApellidoMadre = new JTextField(16);
    private final JTextField txtCedulaRepresentante = new JTextField(16);
    private final JTextField txtNombreRepresentante = new JTextField(16);
    private final JTextField txtApellidoRepresentante = new JTextField(16);
    private final JTextField txtParentesco = new JTextField(16);

    private final JComboBox<String> cbGrado = new JComboBox<>(new String[]{"Primer Año", "Segundo Año", "Tercer Año", "Cuarto Año", "Quinto Año"});
    private final JComboBox<String> cbSeccion = new JComboBox<>(new String[]{"A", "B", "C", "D"});
    private final JButton btnGuardarCambios = UiStyles.primaryButton("Guardar cambios");

    private final Docente docente;
    private boolean modoEdicion = false;

    public RegistrarEstudiantes() {
        this(null);
    }

    public RegistrarEstudiantes(Docente docente) {
        this.docente = docente;
        validarAccesoApertura();
        setTitle("Gestión de estudiantes - Año activo " + AnioEscolarController.getAnioActivo());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(UiStyles.topBar("Registro de estudiantes"), BorderLayout.NORTH);

        JPanel form = buildFormPanel();
        JPanel info = buildInfoPanel();

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(form), info);
        split.setResizeWeight(0.65);
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
        configurarFiltrosNumericos();
    }



    private void configurarFiltrosNumericos() {
        ((AbstractDocument) txtCedula.getDocument()).setDocumentFilter(new NumericDocumentFilter(9));
        ((AbstractDocument) txtCedulaMadre.getDocument()).setDocumentFilter(new NumericDocumentFilter(9));
        ((AbstractDocument) txtCedulaRepresentante.getDocument()).setDocumentFilter(new NumericDocumentFilter(9));

        ((AbstractDocument) txtNombre.getDocument()).setDocumentFilter(new LetterDocumentFilter(60));
        ((AbstractDocument) txtSegundoNombre.getDocument()).setDocumentFilter(new LetterDocumentFilter(60));
        ((AbstractDocument) txtApellido.getDocument()).setDocumentFilter(new LetterDocumentFilter(60));
        ((AbstractDocument) txtSegundoApellido.getDocument()).setDocumentFilter(new LetterDocumentFilter(60));
        ((AbstractDocument) txtNombreMadre.getDocument()).setDocumentFilter(new LetterDocumentFilter(120));
        ((AbstractDocument) txtApellidoMadre.getDocument()).setDocumentFilter(new LetterDocumentFilter(120));
        ((AbstractDocument) txtNombreRepresentante.getDocument()).setDocumentFilter(new LetterDocumentFilter(120));
        ((AbstractDocument) txtApellidoRepresentante.getDocument()).setDocumentFilter(new LetterDocumentFilter(120));
        ((AbstractDocument) txtParentesco.getDocument()).setDocumentFilter(new LetterDocumentFilter(40));
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        UiStyles.card(form);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int r = 0;
        gbc.gridx = 0;
        gbc.gridy = r++;
        gbc.gridwidth = 2;
        form.add(UiStyles.titleLabel("Datos del estudiante"), gbc);
        gbc.gridwidth = 1;
        r = addField(form, gbc, r, "Nombre", txtNombre);
        r = addField(form, gbc, r, "Segundo nombre", txtSegundoNombre);
        r = addField(form, gbc, r, "Apellido", txtApellido);
        r = addField(form, gbc, r, "Segundo apellido", txtSegundoApellido);
        r = addField(form, gbc, r, "Cédula (solo números, 7-9 dígitos)", txtCedula);

        gbc.gridx = 0;
        gbc.gridy = r++;
        gbc.gridwidth = 2;
        form.add(UiStyles.titleLabel("Madre o padre y representante"), gbc);
        gbc.gridwidth = 1;
        r = addField(form, gbc, r, "Cédula madre/padre (obligatoria)", txtCedulaMadre);
        r = addField(form, gbc, r, "Nombre madre/padre (obligatorio)", txtNombreMadre);
        r = addField(form, gbc, r, "Apellido madre/padre (obligatorio)", txtApellidoMadre);
        r = addField(form, gbc, r, "Cédula representante", txtCedulaRepresentante);
        r = addField(form, gbc, r, "Nombre representante", txtNombreRepresentante);
        r = addField(form, gbc, r, "Apellido representante", txtApellidoRepresentante);
        r = addField(form, gbc, r, "Parentesco", txtParentesco);

        gbc.gridx = 0;
        gbc.gridy = r++;
        gbc.gridwidth = 2;
        form.add(UiStyles.titleLabel("Información escolar"), gbc);
        gbc.gridwidth = 1;
        r = addField(form, gbc, r, "Año de estudio", cbGrado);
        r = addField(form, gbc, r, "Sección", cbSeccion);

        gbc.gridx = 0;
        gbc.gridy = r;
        gbc.gridwidth = 2;
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);

        JButton btnRegistrar = UiStyles.primaryButton("Registrar");
        btnRegistrar.addActionListener(e -> registrar());
        actions.add(btnRegistrar);

        boolean permiteEdicion = AccesoController.puedeModificarEstudiantes(docente);
        if (permiteEdicion) {
            JButton btnModificar = UiStyles.primaryButton("Modificar por cédula");
            btnModificar.addActionListener(e -> cargarParaModificar());
            actions.add(btnModificar);

            btnGuardarCambios.setVisible(false);
            btnGuardarCambios.addActionListener(e -> guardarCambios());
            actions.add(btnGuardarCambios);

            JButton btnEliminar = UiStyles.primaryButton("Eliminar por cédula");
            btnEliminar.addActionListener(e -> abrirDialogoEliminar());
            actions.add(btnEliminar);

            if (docente != null && docente.esControlEstudios()) {
                JButton btnAsignaciones = UiStyles.primaryButton("Asignar docentes");
                btnAsignaciones.addActionListener(e -> new AsignacionDocentesControlEstudios().setVisible(true));
                actions.add(btnAsignaciones);
            }
        }

        form.add(actions, gbc);
        return form;
    }

    private JPanel buildInfoPanel() {
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        UiStyles.card(info);

        info.add(UiStyles.titleLabel("Referencia rápida"));
        info.add(UiStyles.subtitleLabel("Diseño adaptable por resolución"));

        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setText("• Datos personales separados de datos escolares y representante.\n"
                + "• Cédula: solo números, entre 7 y 9 dígitos.\n"
                + "• Se solicita madre o padre para registro institucional.\n"
                + "• Control de estudios registra y corrige expedientes.\n"
                + "• Sección y año de estudio forman parte de la inscripción escolar.\n"
                + "• El formulario se adapta al tamaño de ventana.");

        info.add(new JScrollPane(txt));
        return info;
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(comp, gbc);
        return row + 1;
    }

    private void registrar() {
        try {
            validarAccionRegistro();
            EstudianteController.registrarCompleto(
                    txtNombre.getText(), txtSegundoNombre.getText(), txtApellido.getText(), txtSegundoApellido.getText(),
                    txtCedula.getText(), "", cbGrado.getSelectedItem().toString(), cbSeccion.getSelectedItem().toString(),
                    txtCedulaMadre.getText(), txtNombreMadre.getText(), txtApellidoMadre.getText(),
                    txtCedulaRepresentante.getText(), txtNombreRepresentante.getText(), txtApellidoRepresentante.getText(),
                    txtParentesco.getText());
            limpiar();
            JOptionPane.showMessageDialog(this, "Estudiante registrado correctamente.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void cargarParaModificar() {
        String cedula = JOptionPane.showInputDialog(this, "Ingrese la cédula a modificar:");
        if (cedula == null || cedula.isBlank()) {
            return;
        }
        Estudiante e = EstudianteController.buscarPorCedula(cedula);
        if (e == null) {
            JOptionPane.showMessageDialog(this, "No existe estudiante con esa cédula.");
            return;
        }

        txtCedula.setText(e.getCedula());
        txtCedula.setEditable(false);
        txtNombre.setText(e.getNombre());
        txtSegundoNombre.setText(e.getSegundoNombre());
        txtApellido.setText(e.getApellido());
        txtSegundoApellido.setText(e.getSegundoApellido());
        cbGrado.setSelectedItem(e.getGrado());
        cbSeccion.setSelectedItem(e.getSeccion());

        modelo.Representante r = RepresentanteController.buscarPorCedulaEstudiante(e.getCedula());
        if (r != null) {
            txtCedulaMadre.setText(r.getCedulaMadre());
            txtNombreMadre.setText(r.getNombreMadre());
            txtApellidoMadre.setText(r.getApellidoMadre());
            txtCedulaRepresentante.setText(r.getCedulaRepresentante());
            txtNombreRepresentante.setText(r.getNombreRepresentante());
            txtApellidoRepresentante.setText(r.getApellidoRepresentante());
            txtParentesco.setText(r.getParentescoRepresentante());
        }

        modoEdicion = true;
        btnGuardarCambios.setVisible(true);
    }

    private void guardarCambios() {
        if (!modoEdicion) {
            return;
        }
        try {
            AccesoController.validar(docente, AccesoController.puedeModificarEstudiantes(docente),
                    "Solo control de estudios puede modificar expedientes.");
            EstudianteController.actualizar(
                    txtCedula.getText(), txtNombre.getText(), txtSegundoNombre.getText(), txtApellido.getText(),
                    txtSegundoApellido.getText(), "", cbGrado.getSelectedItem().toString(), cbSeccion.getSelectedItem().toString());
            RepresentanteController.registrar(txtCedula.getText(), txtCedulaMadre.getText(), txtNombreMadre.getText(),
                    txtApellidoMadre.getText(), txtCedulaRepresentante.getText(), txtNombreRepresentante.getText(),
                    txtApellidoRepresentante.getText(), txtParentesco.getText());
            JOptionPane.showMessageDialog(this, "Cambios guardados correctamente.");
            limpiar();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void abrirDialogoEliminar() {
        AccesoController.validar(docente, AccesoController.puedeModificarEstudiantes(docente),
                "Solo control de estudios puede eliminar expedientes.");
        JDialog dialog = new JDialog(this, "Eliminar estudiante", true);
        dialog.setSize(360, 160);
        dialog.setLocationRelativeTo(this);

        JTextField txtCedulaEliminar = new JTextField(12);
        ((AbstractDocument) txtCedulaEliminar.getDocument()).setDocumentFilter(new NumericDocumentFilter(9));
        JButton btnEliminar = UiStyles.primaryButton("Eliminar");
        btnEliminar.addActionListener(e -> {
            try {
                EstudianteController.eliminar(txtCedulaEliminar.getText().trim());
                JOptionPane.showMessageDialog(dialog, "Estudiante eliminado correctamente.");
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage());
            }
        });

        JPanel p = new JPanel(new FlowLayout());
        p.add(new JLabel("Cédula:"));
        p.add(txtCedulaEliminar);
        p.add(btnEliminar);
        dialog.add(p);
        dialog.setVisible(true);
    }

    private void limpiar() {
        txtNombre.setText("");
        txtSegundoNombre.setText("");
        txtApellido.setText("");
        txtSegundoApellido.setText("");
        txtCedula.setText("");
        txtCedula.setEditable(true);
        txtCedulaMadre.setText("");
        txtNombreMadre.setText("");
        txtApellidoMadre.setText("");
        txtCedulaRepresentante.setText("");
        txtNombreRepresentante.setText("");
        txtApellidoRepresentante.setText("");
        txtParentesco.setText("");
        cbGrado.setSelectedIndex(0);
        cbSeccion.setSelectedIndex(0);
        modoEdicion = false;
        btnGuardarCambios.setVisible(false);
    }

    private void validarAccesoApertura() {
        AccesoController.validar(docente, AccesoController.puedeGestionarEstudiantes(docente),
                "Solo control de estudios o dirección pueden abrir el módulo de estudiantes.");
    }

    private void validarAccionRegistro() {
        AccesoController.validar(docente, AccesoController.puedeGestionarEstudiantes(docente),
                "Solo control de estudios o dirección pueden registrar estudiantes.");
    }
}
