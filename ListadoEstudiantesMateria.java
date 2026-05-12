package Vista;

import controlador.AccesoController;
import controlador.AsignacionDocenteController;
import controlador.EstudianteController;
import controlador.InscripcionController;
import controlador.SessionContext;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import modelo.AsignacionDocente;
import modelo.Docente;
import modelo.Estudiante;

public class ListadoEstudiantesMateria extends JFrame {

    private final JComboBox<String> cbAnio = new JComboBox<>();
    private final JComboBox<String> cbSeccion = new JComboBox<>(new String[]{"A", "B", "C", "D"});
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Cédula", "Nombre", "Año", "Sección"}, 0);
    private final Docente usuario = SessionContext.getUsuarioActual();
    private final Map<String, java.util.Set<String>> seccionesPorAnioDocente = new LinkedHashMap<>();
    private final JLabel lblResumen = new JLabel("Resultados: 0 estudiantes");

    public ListadoEstudiantesMateria() {
        setTitle("Listado por sección");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);
        validarAcceso();

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));

        root.add(UiStyles.topBar("Estudiantes por sección"), BorderLayout.NORTH);

        JPanel topFilter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        UiStyles.card(topFilter);
        topFilter.add(new JLabel("Año:"));
        topFilter.add(cbAnio);
        topFilter.add(new JLabel("Sección:"));
        topFilter.add(cbSeccion);
        JButton btnBuscar = UiStyles.primaryButton("Ver sección");
        btnBuscar.addActionListener(e -> cargar());
        topFilter.add(btnBuscar);

        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(26);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(topFilter, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(lblResumen, BorderLayout.SOUTH);

        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
        cbAnio.addActionListener(e -> onCambioAnio());
        configurarFiltrosPorRol();
        cargar();
    }

    private void validarAcceso() {
        boolean permitido = usuario != null && (usuario.esDocente() || AccesoController.puedeGestionarEstudiantes(usuario));
        AccesoController.validar(usuario, permitido,
                "Solo docente, control de estudios o dirección pueden consultar el listado por sección.");
    }

    private void configurarFiltrosPorRol() {
        if (usuario != null && usuario.esDocente()) {
            List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(usuario.getId());
            for (AsignacionDocente asignacion : asignaciones) {
                seccionesPorAnioDocente.computeIfAbsent(asignacion.getAnioEstudio(), k -> new java.util.LinkedHashSet<>())
                        .add(asignacion.getSeccion());
            }
            cbAnio.setModel(new DefaultComboBoxModel<>(seccionesPorAnioDocente.keySet().toArray(String[]::new)));
            onCambioAnio();
            return;
        }

        cbAnio.setModel(new DefaultComboBoxModel<>(InscripcionController.ANIOS_ESTUDIO_VALIDOS.toArray(String[]::new)));
        cbSeccion.setModel(new DefaultComboBoxModel<>(InscripcionController.SECCIONES_VALIDAS.toArray(String[]::new)));
    }

    private void onCambioAnio() {
        if (!(usuario != null && usuario.esDocente())) {
            return;
        }
        String anio = (String) cbAnio.getSelectedItem();
        java.util.Set<String> secciones = anio == null
                ? java.util.Set.of()
                : seccionesPorAnioDocente.getOrDefault(anio, java.util.Set.of());
        cbSeccion.setModel(new DefaultComboBoxModel<>(secciones.toArray(String[]::new)));
    }

    private void cargar() {
        model.setRowCount(0);
        String anio = cbAnio.getSelectedItem() == null ? "" : cbAnio.getSelectedItem().toString();
        if (cbSeccion.getSelectedItem() == null) {
            return;
        }
        String seccion = cbSeccion.getSelectedItem().toString();
        if (usuario != null && usuario.esDocente()) {
            java.util.Set<String> seccionesPermitidas = seccionesPorAnioDocente.getOrDefault(anio, java.util.Set.of());
            if (!seccionesPermitidas.contains(seccion)) {
                JOptionPane.showMessageDialog(this, "Solo puede consultar años/secciones donde tiene asignación docente.");
                return;
            }
        }
        for (Estudiante e : EstudianteController.listarPorAnioYSeccion(anio, seccion)) {
            model.addRow(new Object[]{e.getCedula(), e.getNombreCompleto(), e.getGrado(), e.getSeccion()});
        }
        lblResumen.setText("Resultados: " + model.getRowCount() + " estudiantes");
    }
}
