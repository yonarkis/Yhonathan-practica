package Vista;

import controlador.AccesoController;
import controlador.AsignacionDocenteController;
import controlador.AsistenciaController;
import controlador.EstudianteController;
import controlador.SessionContext;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.AsignacionDocente;
import modelo.Docente;
import modelo.Estudiante;

public class ResumenAsistenciaDocente extends JFrame {

    private final JComboBox<AsignacionDocente> cbAsignacion = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Cédula", "Estudiante", "Presentes", "Ausentes", "Justificados"}, 0);

    public ResumenAsistenciaDocente() {
        setTitle("Resumen de asistencia");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        validarAcceso();

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(UiStyles.topBar("Consulta de asistencia por grupo"), BorderLayout.NORTH);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        UiStyles.card(filtros);
        filtros.add(new JLabel("Grupo:"));
        filtros.add(cbAsignacion);
        JButton btn = UiStyles.primaryButton("Actualizar resumen");
        btn.addActionListener(e -> cargarResumen());
        filtros.add(btn);

        JTable tabla = new JTable(model);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        north.add(filtros, BorderLayout.CENTER);

        root.add(north, BorderLayout.NORTH);
        root.add(new JScrollPane(tabla), BorderLayout.CENTER);
        setContentPane(root);

        cargarAsignaciones();
    }

    private void validarAcceso() {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, usuario != null && usuario.esDocente(),
                "Solo un docente puede abrir el resumen de asistencia.");
    }

    private void cargarAsignaciones() {
        Docente usuario = SessionContext.getUsuarioActual();
        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(usuario.getId());
        cbAsignacion.setModel(new DefaultComboBoxModel<>(asignaciones.toArray(AsignacionDocente[]::new)));
        cbAsignacion.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getEtiqueta()));
        cbAsignacion.addActionListener(e -> cargarResumen());
        cargarResumen();
    }

    private void cargarResumen() {
        model.setRowCount(0);
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        Docente usuario = SessionContext.getUsuarioActual();
        if (asignacion == null || usuario == null) {
            return;
        }
        Map<String, long[]> resumen = AsistenciaController.resumenPorGrupo(
                asignacion.getAnioEstudio(), asignacion.getSeccion(), usuario.getUsuario());
        for (Estudiante estudiante : EstudianteController.listarPorAnioYSeccion(asignacion.getAnioEstudio(), asignacion.getSeccion())) {
            long[] contador = resumen.getOrDefault(estudiante.getCedula(), new long[3]);
            model.addRow(new Object[]{
                estudiante.getCedula(),
                estudiante.getNombreCompleto(),
                contador[0],
                contador[1],
                contador[2]
            });
        }
    }
}
