package Vista;

import controlador.AccesoController;
import controlador.AsignacionDocenteController;
import controlador.EstudianteController;
import controlador.ObservacionDocenteController;
import controlador.SessionContext;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import modelo.AsignacionDocente;
import modelo.Docente;
import modelo.Estudiante;

public class ObservacionesDocente extends JFrame {

    private final JComboBox<AsignacionDocente> cbAsignacion = new JComboBox<>();
    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final JComboBox<String> cbTipo = new JComboBox<>(ObservacionDocenteController.TIPOS.toArray(String[]::new));
    private final JTextArea txtDetalle = new JTextArea(6, 32);
    private final JLabel lblResumen = new JLabel("Seleccione un grupo y un estudiante para registrar la observación.");

    public ObservacionesDocente() {
        setTitle("Observaciones del docente");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        validarAcceso();

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(UiStyles.topBar("Observaciones y seguimiento docente"), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        setContentPane(root);

        cargarAsignaciones();
    }

    private void validarAcceso() {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, usuario != null && usuario.esDocente(),
                "Solo un docente puede registrar observaciones.");
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        UiStyles.card(panel);

        txtDetalle.setLineWrap(true);
        txtDetalle.setWrapStyleWord(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        row = addField(panel, gbc, row, "Grupo asignado", cbAsignacion);
        row = addField(panel, gbc, row, "Estudiante", cbEstudiante);
        row = addField(panel, gbc, row, "Tipo", cbTipo);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Detalle:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(new JScrollPane(txtDetalle), gbc);
        row++;

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(lblResumen, gbc);
        gbc.gridwidth = 1;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton btnGuardar = UiStyles.primaryButton("Guardar observación");
        btnGuardar.addActionListener(e -> guardarObservacion());
        actions.add(btnGuardar);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(actions, gbc);

        cbAsignacion.addActionListener(e -> cargarEstudiantes());
        cbEstudiante.addActionListener(e -> actualizarResumen());
        return panel;
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(comp, gbc);
        return row + 1;
    }

    private void cargarAsignaciones() {
        Docente usuario = SessionContext.getUsuarioActual();
        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(usuario.getId());
        cbAsignacion.setModel(new DefaultComboBoxModel<>(asignaciones.toArray(AsignacionDocente[]::new)));
        cbAsignacion.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getEtiqueta()));
        cargarEstudiantes();
    }

    private void cargarEstudiantes() {
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        if (asignacion == null) {
            cbEstudiante.setModel(new DefaultComboBoxModel<>());
            lblResumen.setText("No hay grupos asignados para este docente.");
            return;
        }
        List<Estudiante> estudiantes = EstudianteController.listarPorAnioYSeccion(
                asignacion.getAnioEstudio(), asignacion.getSeccion());
        cbEstudiante.setModel(new DefaultComboBoxModel<>(estudiantes.toArray(Estudiante[]::new)));
        cbEstudiante.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getNombreCompleto() + " | " + value.getCedula()));
        actualizarResumen();
    }

    private void actualizarResumen() {
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        Estudiante estudiante = (Estudiante) cbEstudiante.getSelectedItem();
        if (asignacion == null) {
            lblResumen.setText("No hay grupos asignados para este docente.");
            return;
        }
        if (estudiante == null) {
            lblResumen.setText("Grupo: " + asignacion.getEtiqueta() + " | Sin estudiantes disponibles.");
            return;
        }
        lblResumen.setText("Grupo: " + asignacion.getEtiqueta() + " | Estudiante: "
                + estudiante.getNombreCompleto() + " | C.I.: " + estudiante.getCedula());
    }

    private void guardarObservacion() {
        Estudiante estudiante = (Estudiante) cbEstudiante.getSelectedItem();
        if (estudiante == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un estudiante.");
            return;
        }
        try {
            ObservacionDocenteController.registrar(
                    estudiante.getCedula(),
                    cbTipo.getSelectedItem().toString(),
                    txtDetalle.getText());
            txtDetalle.setText("");
            JOptionPane.showMessageDialog(this, "Observación registrada correctamente.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
