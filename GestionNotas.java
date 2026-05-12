package Vista;

import controlador.AccesoController;
import controlador.AsignacionDocenteController;
import controlador.EstudianteController;
import controlador.InscripcionController;
import controlador.LapsoController;
import controlador.NotaController;
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
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.text.AbstractDocument;
import modelo.AsignacionDocente;
import modelo.Docente;
import modelo.Estudiante;
import modelo.InscripcionEscolar;

public class GestionNotas extends JFrame {

    private final JTextField txtCedula = new JTextField(14);
    private final JComboBox<AsignacionDocente> cbAsignacion = new JComboBox<>();
    private final JComboBox<Estudiante> cbEstudiante = new JComboBox<>();
    private final JComboBox<String> cbMateria = new JComboBox<>(NotaController.MATERIAS.toArray(String[]::new));
    private final JComboBox<Integer> cbLapso = new JComboBox<>(new Integer[]{1, 2, 3});
    private final JTextField[] notasFields = new JTextField[]{
        new JTextField(4), new JTextField(4), new JTextField(4), new JTextField(4), new JTextField(4)
    };
    private final JLabel lblEstudiante = new JLabel("Estudiante: -");
    private final Docente usuarioActual = SessionContext.getUsuarioActual();
    private final boolean modoConsultaControlEstudios = usuarioActual != null && usuarioActual.esControlEstudios();
    private JButton btnGuardar;
    private Estudiante estudianteConsultaControl;

    public GestionNotas() {
        setTitle("Gestión de notas");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(UiStyles.topBar("Gestión de notas por estudiante"), BorderLayout.NORTH);
        root.add(buildForm(), BorderLayout.CENTER);
        setContentPane(root);
        configurarFiltrosNumericos();
        validarAcceso();
        configurarContextoDocente();
    }

    private void configurarFiltrosNumericos() {
        ((AbstractDocument) txtCedula.getDocument()).setDocumentFilter(new NumericDocumentFilter(9));
        for (JTextField campo : notasFields) {
            ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DecimalDocumentFilter(4, 20.0));
        }
    }

    private void validarAcceso() {
        AccesoController.validar(usuarioActual,
                AccesoController.puedeGestionarNotas(usuarioActual),
                "Solo docente o control de estudios pueden gestionar notas.");
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        UiStyles.card(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        if (usuarioActual != null && usuarioActual.esDocente()) {
            row = addField(panel, gbc, row, "Grupo asignado", cbAsignacion);
            row = addField(panel, gbc, row, "Estudiante del grupo", cbEstudiante);
        }

        row = addField(panel, gbc, row, "Cédula", txtCedula);
        JButton btnBuscar = UiStyles.primaryButton("Buscar estudiante");
        btnBuscar.addActionListener(e -> buscarEstudiante());
        gbc.gridx = 1;
        gbc.gridy = row++;
        panel.add(btnBuscar, gbc);

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(lblEstudiante, gbc);
        gbc.gridwidth = 1;

        row = addField(panel, gbc, row, "Materia", cbMateria);
        row = addField(panel, gbc, row, "Lapso", cbLapso);

        JPanel panelNotas = new JPanel(new GridBagLayout());
        panelNotas.setOpaque(false);
        panelNotas.setBorder(BorderFactory.createTitledBorder("Evaluaciones del lapso"));
        GridBagConstraints gNotas = new GridBagConstraints();
        gNotas.insets = new Insets(4, 4, 4, 4);
        gNotas.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < notasFields.length; i++) {
            gNotas.gridx = 0;
            gNotas.gridy = i;
            panelNotas.add(new JLabel("Nota " + (i + 1) + ":"), gNotas);
            gNotas.gridx = 1;
            panelNotas.add(notasFields[i], gNotas);
        }
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(panelNotas, gbc);
        gbc.gridwidth = 1;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnGuardar = UiStyles.primaryButton(modoConsultaControlEstudios ? "Modo consulta" : "Guardar notas");
        btnGuardar.addActionListener(e -> guardarNotas());
        if (modoConsultaControlEstudios) {
            btnGuardar.setEnabled(false);
            btnGuardar.setToolTipText("Control de Estudios consulta notas en modo solo lectura.");
        }
        btnPanel.add(btnGuardar);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

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

    private void configurarContextoDocente() {
        if (usuarioActual == null || !usuarioActual.esDocente()) {
            if (modoConsultaControlEstudios) {
                cbLapso.setSelectedItem(LapsoController.getLapsoActivo());
                cbMateria.addActionListener(e -> cargarNotasConsultaControl());
                cbLapso.addActionListener(e -> cargarNotasConsultaControl());
                for (JTextField campo : notasFields) {
                    campo.setEditable(false);
                }
            }
            return;
        }

        txtCedula.setEditable(false);
        cbMateria.setEnabled(false);
        cbAsignacion.addActionListener(e -> cargarEstudiantesPorAsignacion());
        cbEstudiante.addActionListener(e -> sincronizarEstudianteSeleccionado());
        cbAsignacion.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getEtiqueta()));
        cbEstudiante.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getNombreCompleto() + " | " + value.getCedula()));

        cbLapso.setSelectedItem(LapsoController.getLapsoActivo());
        cbLapso.setEnabled(false);

        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(usuarioActual.getId());
        cbAsignacion.setModel(new DefaultComboBoxModel<>(asignaciones.toArray(AsignacionDocente[]::new)));
        cargarEstudiantesPorAsignacion();
    }

    private void cargarEstudiantesPorAsignacion() {
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        if (asignacion == null) {
            cbEstudiante.setModel(new DefaultComboBoxModel<>());
            txtCedula.setText("");
            lblEstudiante.setText("Estudiante: no hay grupos asignados.");
            return;
        }

        cbMateria.setSelectedItem(asignacion.getMateria());
        List<Estudiante> estudiantes = EstudianteController.listarPorAnioYSeccion(
                asignacion.getAnioEstudio(), asignacion.getSeccion());
        cbEstudiante.setModel(new DefaultComboBoxModel<>(estudiantes.toArray(Estudiante[]::new)));
        sincronizarEstudianteSeleccionado();
    }

    private void sincronizarEstudianteSeleccionado() {
        Estudiante estudiante = (Estudiante) cbEstudiante.getSelectedItem();
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        if (estudiante == null || asignacion == null) {
            txtCedula.setText("");
            lblEstudiante.setText("Estudiante: seleccione un grupo y un alumno.");
            return;
        }

        txtCedula.setText(estudiante.getCedula());
        lblEstudiante.setText("Estudiante: " + estudiante.getNombreCompleto()
                + " | " + asignacion.getAnioEstudio() + " - Sección " + asignacion.getSeccion());
        cargarNotasExistentes(estudiante);
    }


    private void buscarEstudiante() {
        String cedula = txtCedula.getText().trim();
        if (!cedula.matches("\\d{7,9}")) {
            lblEstudiante.setText("Estudiante: cédula inválida (solo números, 7 a 9 dígitos)");
            return;
        }
        Estudiante estudiante = EstudianteController.buscarPorCedula(cedula);
        if (estudiante == null) {
            estudianteConsultaControl = null;
            lblEstudiante.setText("Estudiante: no encontrado");
            limpiarNotas();
            return;
        }
        modelo.InscripcionEscolar i = InscripcionController.buscarEnAnioActivo(estudiante.getCedula());
        String escolar = i == null ? "Sin inscripción activa" : (i.getAnioEstudio() + " - Sección " + i.getSeccion());
        lblEstudiante.setText("Estudiante: " + estudiante.getNombreCompleto() + " | " + escolar);
        if (modoConsultaControlEstudios) {
            estudianteConsultaControl = estudiante;
            cargarMateriasSegunAnio(i);
            cargarNotasConsultaControl();
            return;
        }
        if (usuarioActual != null && usuarioActual.esDocente()) {
            validarAsignacionDocente(estudiante, cbMateria.getSelectedItem().toString());
        }
    }

    private String validarCedula() {
        String cedula = txtCedula.getText().trim();
        if (!cedula.matches("\\d{7,9}")) {
            throw new IllegalArgumentException("La cédula debe tener solo números y 7 a 9 dígitos.");
        }
        return cedula;
    }

    private void guardarNotas() {
        try {
            Estudiante estudianteObjetivo = null;
            if (modoConsultaControlEstudios) {
                throw new IllegalArgumentException("Control de Estudios está en modo consulta de notas (solo lectura).");
            }
            if (usuarioActual != null && usuarioActual.esDocente()) {
                estudianteObjetivo = (Estudiante) cbEstudiante.getSelectedItem();
                if (estudianteObjetivo == null) {
                    throw new IllegalArgumentException("Debe seleccionar un estudiante de su grupo.");
                }
                validarAsignacionDocente(estudianteObjetivo, cbMateria.getSelectedItem().toString());
            }

            java.util.List<Double> notas = new java.util.ArrayList<>();
            for (JTextField campo : notasFields) {
                String valor = campo.getText().trim();
                if (valor.isBlank()) {
                    continue;
                }
                if (!valor.matches("\\d{1,2}(\\.\\d)?")) {
                    throw new IllegalArgumentException("Cada nota debe tener formato válido (ejemplo: 0.1, 9.5, 20).");
                }
                double notaValor = Double.parseDouble(valor);
                if (notaValor < 0.1 || notaValor > 20) {
                    throw new IllegalArgumentException("Cada nota debe estar entre 0.1 y 20.");
                }
                notas.add(notaValor);
            }
            NotaController.guardarNotasPorCedula(
                    validarCedula(),
                    cbMateria.getSelectedItem().toString(),
                    (Integer) cbLapso.getSelectedItem(),
                    notas);
            JOptionPane.showMessageDialog(this, "Notas guardadas correctamente.");
            if (estudianteObjetivo != null) {
                cargarNotasExistentes(estudianteObjetivo);
            } else {
                for (JTextField campo : notasFields) {
                    campo.setText("");
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Las notas deben ser numéricas.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void cargarMateriasSegunAnio(InscripcionEscolar inscripcion) {
        if (inscripcion == null) {
            cbMateria.setModel(new DefaultComboBoxModel<>());
            return;
        }
        List<String> materias = AsignacionDocenteController.materiasParaAnio(inscripcion.getAnioEstudio());
        cbMateria.setModel(new DefaultComboBoxModel<>(materias.toArray(String[]::new)));
    }

    private void cargarNotasConsultaControl() {
        if (!modoConsultaControlEstudios) {
            return;
        }
        if (estudianteConsultaControl == null || cbMateria.getSelectedItem() == null) {
            limpiarNotas();
            return;
        }
        cargarNotasExistentes(estudianteConsultaControl);
        for (JTextField campo : notasFields) {
            campo.setEditable(false);
            campo.setToolTipText("Consulta de notas: edición bloqueada para control de estudios.");
        }
    }

    private void limpiarNotas() {
        for (JTextField campo : notasFields) {
            campo.setText("");
            campo.setEditable(!modoConsultaControlEstudios);
            campo.setToolTipText(null);
        }
    }


    private void cargarNotasExistentes(Estudiante estudiante) {
        int lapso = (Integer) cbLapso.getSelectedItem();
        String materia = cbMateria.getSelectedItem().toString();
        java.util.List<modelo.Nota> existentes = NotaController.getNotas().stream()
                .filter(n -> n.getIdEstudiante() == estudiante.getId()
                        && n.getMateria().equalsIgnoreCase(materia)
                        && n.getAnioEscolar().equalsIgnoreCase(controlador.AnioEscolarController.getAnioActivo())
                        && n.getLapso() == lapso)
                .sorted(java.util.Comparator.comparingInt(modelo.Nota::getNumeroEvaluacion))
                .toList();

        for (int i = 0; i < notasFields.length; i++) {
            if (i < existentes.size()) {
                notasFields[i].setText(String.format("%.1f", existentes.get(i).getNota()));
                notasFields[i].setEditable(false);
                notasFields[i].setToolTipText("Nota ya cargada. Las correcciones se gestionan por Control de Estudios.");
            } else {
                notasFields[i].setText("");
                notasFields[i].setEditable(true);
                notasFields[i].setToolTipText(null);
            }
        }
    }

    private void validarAsignacionDocente(Estudiante estudiante, String materia) {
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        if (asignacion == null) {
            throw new IllegalArgumentException("No tiene grupos asignados en el año activo.");
        }
        boolean estudiantePertenece = estudiante.getGrado().equalsIgnoreCase(asignacion.getAnioEstudio())
                && estudiante.getSeccion().equalsIgnoreCase(asignacion.getSeccion());
        boolean materiaAsignada = asignacion.getMateria().equalsIgnoreCase(materia);
        if (!estudiantePertenece || !materiaAsignada) {
            throw new IllegalArgumentException("Solo puede cargar notas de sus grupos y materias asignadas.");
        }
    }
}
