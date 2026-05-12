package Vista;

import controlador.AccesoController;
import controlador.AsignacionDocenteController;
import controlador.AsistenciaController;
import controlador.EstudianteController;
import controlador.SessionContext;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import modelo.AsignacionDocente;
import modelo.Docente;
import modelo.Estudiante;

public class ControlAsistencia extends JFrame {

    private final JComboBox<AsignacionDocente> cbAsignacion = new JComboBox<>();
    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final JComboBox<String> cbEstado = new JComboBox<>(AsistenciaController.ESTADOS.toArray(String[]::new));
    private final JSpinner spFecha = new JSpinner(new SpinnerDateModel());
    private final JLabel lblResumen = new JLabel("Seleccione una asignación para cargar asistencia.");

    public ControlAsistencia() {
        setTitle("Control de asistencia");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        validarAcceso();

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(UiStyles.topBar("Control de asistencia docente"), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        setContentPane(root);

        JSpinner.DateEditor editorFecha = new JSpinner.DateEditor(spFecha, "yyyy-MM-dd");
        spFecha.setEditor(editorFecha);
        cargarAsignaciones();
    }

    private void validarAcceso() {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, usuario != null && usuario.esDocente(),
                "Solo un docente puede abrir el módulo de asistencia.");
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        UiStyles.card(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        row = addField(panel, gbc, row, "Grupo asignado", cbAsignacion);
        row = addField(panel, gbc, row, "Estudiante", cbEstudiante);
        row = addField(panel, gbc, row, "Estado", cbEstado);
        row = addField(panel, gbc, row, "Fecha clase", spFecha);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(lblResumen, gbc);
        gbc.gridwidth = 1;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton btnGuardar = UiStyles.primaryButton("Guardar asistencia");
        btnGuardar.addActionListener(e -> guardarAsistencia());
        actions.add(btnGuardar);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(actions, gbc);

        cbAsignacion.addActionListener(e -> cargarEstudiantes());
        return panel;
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
            lblResumen.setText("No hay asignaciones cargadas para este docente.");
            return;
        }

        List<Estudiante> estudiantes = EstudianteController.listarPorAnioYSeccion(
                asignacion.getAnioEstudio(), asignacion.getSeccion());
        cbEstudiante.setModel(new DefaultComboBoxModel<>(estudiantes.toArray(Estudiante[]::new)));
        cbEstudiante.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getNombreCompleto() + " | " + value.getCedula()));
        lblResumen.setText("Grupo: " + asignacion.getEtiqueta() + " | Estudiantes: " + estudiantes.size());
    }

    private void guardarAsistencia() {
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        Estudiante estudiante = (Estudiante) cbEstudiante.getSelectedItem();
        if (asignacion == null || estudiante == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar grupo y estudiante.");
            return;
        }
        try {
            AsistenciaController.registrar(
                    estudiante.getCedula(),
                    asignacion.getAnioEstudio(),
                    asignacion.getSeccion(),
                    cbEstado.getSelectedItem().toString(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format((Date) spFecha.getValue()));
            JOptionPane.showMessageDialog(this, "Asistencia registrada correctamente.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
