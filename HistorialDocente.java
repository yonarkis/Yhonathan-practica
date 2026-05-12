package Vista;

import controlador.AccesoController;
import controlador.EstudianteController;
import controlador.AsignacionDocenteController;
import controlador.NotaController;
import controlador.ObservacionDocenteController;
import controlador.SessionContext;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import modelo.AsignacionDocente;
import modelo.Docente;
import modelo.ObservacionDocente;

public class HistorialDocente extends JFrame {

    private final JComboBox<AsignacionDocente> cbAsignacion = new JComboBox<>();
    private final DefaultTableModel observacionesModel = new DefaultTableModel(
            new String[]{"Fecha", "Estudiante", "Tipo", "Detalle"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTextArea txtAlertas = new JTextArea();

    public HistorialDocente() {
        setTitle("Historial docente");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        validarAcceso();

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(UiStyles.topBar("Historial, observaciones y alertas"), BorderLayout.NORTH);
        root.add(buildContent(), BorderLayout.CENTER);
        setContentPane(root);

        cargarAsignaciones();
        refrescarContenido();
    }

    private void validarAcceso() {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, usuario != null && usuario.esDocente(),
                "Solo un docente puede consultar su historial.");
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setOpaque(false);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        UiStyles.card(filtros);
        filtros.add(new JLabel("Grupo:"));
        filtros.add(cbAsignacion);
        JButton btnActualizar = UiStyles.primaryButton("Actualizar historial");
        btnActualizar.addActionListener(e -> refrescarContenido());
        filtros.add(btnActualizar);
        content.add(filtros, BorderLayout.NORTH);

        JTable tablaObservaciones = new JTable(observacionesModel);
        tablaObservaciones.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        txtAlertas.setEditable(false);
        txtAlertas.setLineWrap(true);
        txtAlertas.setWrapStyleWord(true);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Observaciones", new JScrollPane(tablaObservaciones));
        tabs.addTab("Alertas académicas", new JScrollPane(txtAlertas));
        content.add(tabs, BorderLayout.CENTER);
        return content;
    }

    private void cargarAsignaciones() {
        Docente usuario = SessionContext.getUsuarioActual();
        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(usuario.getId());
        cbAsignacion.setModel(new DefaultComboBoxModel<>(asignaciones.toArray(AsignacionDocente[]::new)));
        cbAsignacion.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getEtiqueta()));
        cbAsignacion.addActionListener(e -> refrescarContenido());
    }

    private void refrescarContenido() {
        cargarObservaciones();
        cargarAlertas();
    }

    private void cargarObservaciones() {
        observacionesModel.setRowCount(0);
        Docente usuario = SessionContext.getUsuarioActual();
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        Set<String> cedulasGrupo = asignacion == null
                ? Set.of()
                : EstudianteController.listarPorAnioYSeccion(asignacion.getAnioEstudio(), asignacion.getSeccion()).stream()
                        .map(estudiante -> estudiante.getCedula())
                        .collect(Collectors.toSet());

        for (ObservacionDocente observacion : ObservacionDocenteController.listarPorDocente(usuario.getUsuario())) {
            if (!cedulasGrupo.isEmpty() && !cedulasGrupo.contains(observacion.getCedulaEstudiante())) {
                continue;
            }
            String estudiante = observacion.getCedulaEstudiante();
            modelo.Estudiante datos = EstudianteController.buscarPorCedula(observacion.getCedulaEstudiante());
            if (datos != null) {
                estudiante = datos.getNombreCompleto() + " | " + datos.getCedula();
            }
            observacionesModel.addRow(new Object[]{
                observacion.getFechaHora(),
                estudiante,
                observacion.getTipo(),
                observacion.getDetalle()
            });
        }
    }

    private void cargarAlertas() {
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        if (asignacion == null) {
            txtAlertas.setText("No hay grupos asignados para mostrar alertas.");
            return;
        }
        List<String> alertas = NotaController.alertasAcademicasPorGrupo(
                asignacion.getAnioEstudio(), asignacion.getSeccion(), asignacion.getMateria());
        txtAlertas.setText(alertas.isEmpty()
                ? "Sin alertas académicas para el grupo seleccionado."
                : String.join("\n", alertas));
        txtAlertas.setCaretPosition(0);
    }
}
