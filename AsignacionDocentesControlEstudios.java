package Vista;

import controlador.AccesoController;
import controlador.AsignacionDocenteController;
import controlador.DemoDataController;
import controlador.DocenteController;
import controlador.EstudianteController;
import controlador.InscripcionController;
import controlador.LapsoController;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;
import java.util.Date;
import modelo.AsignacionDocente;
import modelo.Docente;
import modelo.Estudiante;

public class AsignacionDocentesControlEstudios extends JFrame {

    private final JComboBox<String> cbAnio = new JComboBox<>(InscripcionController.ANIOS_ESTUDIO_VALIDOS.toArray(String[]::new));
    private final JComboBox<String> cbSeccion = new JComboBox<>(new String[]{"A", "B", "C", "D"});
    private final JComboBox<String> cbMateria = new JComboBox<>();
    private final JComboBox<Docente> cbDocente = new JComboBox<>();
    private final JComboBox<Docente> cbDocenteGuia = new JComboBox<>();
    private final JComboBox<Integer> cbLapsoActivo = new JComboBox<>(new Integer[]{1, 2, 3});
    private final JComboBox<String> cbDiaSemana = new JComboBox<>(new String[]{"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"});
    private final JComboBox<String> cbHoraInicio = new JComboBox<>();
    private final JComboBox<String> cbHoraFin = new JComboBox<>();
    private final JSpinner spLapsoInicio = new JSpinner(new SpinnerDateModel());
    private final JSpinner spLapsoFin = new JSpinner(new SpinnerDateModel());
    private final JLabel lblDisponibilidad = new JLabel("Disponibilidad: -");

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Docente", "Año", "Sección", "Materia", "Horario"}, 0);
    private final JLabel lblResumenSeccion = new JLabel("Sección: -");
    private final DefaultTableModel modelEstudiantes = new DefaultTableModel(
            new String[]{"Cédula", "Nombre completo"}, 0);
    private final DefaultTableModel modelMateriasSeccion = new DefaultTableModel(
            new String[]{"Materia", "Docentes asignados"}, 0);

    public AsignacionDocentesControlEstudios() {
        setTitle("Asignación docente por curso");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        validarAcceso();

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(UiStyles.topBar("Asignación de profesores y profesor guía"), BorderLayout.NORTH);

        JPanel filtros = buildFiltrosPanel();

        JTable tabla = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tabla), buildDetalleSeccionPanel());
        split.setResizeWeight(0.50);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        JScrollPane filtrosScroll = new JScrollPane(filtros,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        filtrosScroll.setBorder(null);
        center.add(filtrosScroll, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
        spLapsoInicio.setEditor(new JSpinner.DateEditor(spLapsoInicio, "yyyy-MM-dd"));
        spLapsoFin.setEditor(new JSpinner.DateEditor(spLapsoFin, "yyyy-MM-dd"));
        cargarOpcionesHorario();

        cbAnio.addActionListener(e -> recargarContexto());
        cbSeccion.addActionListener(e -> recargarContexto());
        cbMateria.addActionListener(e -> cargarDocentesFiltrados());
        cbDocente.addActionListener(e -> actualizarDisponibilidad());

        cargarMaterias();
        cargarDocentesGuia();
        cargarDocentesFiltrados();
        refrescarTabla();
        refrescarDetalleSeccion(false);
    }

    private JPanel buildDetalleSeccionPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new java.awt.Color(248, 252, 255));
        panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(205, 220, 230)),
                javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        lblResumenSeccion.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        lblResumenSeccion.setForeground(UiStyles.COLOR_PRIMARY_DARK);

        JTable tablaEstudiantes = new JTable(modelEstudiantes) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tablaMaterias = new JTable(modelMateriasSeccion) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Estudiantes", new JScrollPane(tablaEstudiantes));
        tabs.addTab("Materias / Docentes", new JScrollPane(tablaMaterias));

        panel.add(lblResumenSeccion, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFiltrosPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        UiStyles.card(wrapper);

        JPanel filaSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaSuperior.setOpaque(false);
        filaSuperior.add(new JLabel("Año:"));
        filaSuperior.add(cbAnio);
        filaSuperior.add(new JLabel("Sección:"));
        filaSuperior.add(cbSeccion);
        JButton btnBuscar = UiStyles.primaryButton("Buscar sección");
        btnBuscar.addActionListener(e -> refrescarDetalleSeccion(true));
        filaSuperior.add(btnBuscar);
        filaSuperior.add(new JLabel("Lapso activo:"));
        cbLapsoActivo.setSelectedItem(LapsoController.getLapsoActivo());
        filaSuperior.add(cbLapsoActivo);
        JButton btnLapso = UiStyles.primaryButton("Actualizar lapso");
        btnLapso.addActionListener(e -> actualizarLapsoActivo());
        filaSuperior.add(btnLapso);

        JPanel filaLapso = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaLapso.setOpaque(false);
        filaLapso.add(new JLabel("Inicio lapso (YYYY-MM-DD):"));
        filaLapso.add(spLapsoInicio);
        filaLapso.add(new JLabel("Fin lapso (YYYY-MM-DD):"));
        filaLapso.add(spLapsoFin);
        JButton btnRangoLapso = UiStyles.primaryButton("Guardar rango");
        btnRangoLapso.addActionListener(e -> configurarRangoLapso());
        filaLapso.add(btnRangoLapso);

        JPanel filaInferior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filaInferior.setOpaque(false);
        filaInferior.add(new JLabel("Docente guía:"));
        filaInferior.add(cbDocenteGuia);
        JButton btnGuia = UiStyles.primaryButton("Asignar guía");
        btnGuia.addActionListener(e -> asignarGuia());
        filaInferior.add(btnGuia);
        filaInferior.add(new JLabel("Materia:"));
        filaInferior.add(cbMateria);
        filaInferior.add(new JLabel("Docente materia:"));
        filaInferior.add(cbDocente);
        filaInferior.add(new JLabel("Día:"));
        filaInferior.add(cbDiaSemana);
        filaInferior.add(new JLabel("Inicio:"));
        filaInferior.add(cbHoraInicio);
        filaInferior.add(new JLabel("Fin:"));
        filaInferior.add(cbHoraFin);

        JButton btnAsignar = UiStyles.primaryButton("Asignar materia");
        btnAsignar.addActionListener(e -> asignarMateria());
        filaInferior.add(btnAsignar);
        filaInferior.add(lblDisponibilidad);

        JPanel extras = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        extras.setOpaque(false);
        JButton btnRegenerar = UiStyles.primaryButton("Regenerar demo");
        btnRegenerar.addActionListener(e -> regenerarDemo());
        extras.add(btnRegenerar);
        JButton btnVaciar = UiStyles.primaryButton("Vaciar demo");
        btnVaciar.addActionListener(e -> vaciarDemo());
        extras.add(btnVaciar);

        JPanel stacked = new JPanel(new BorderLayout(0, 4));
        stacked.setOpaque(false);
        stacked.add(filaSuperior, BorderLayout.NORTH);
        stacked.add(filaLapso, BorderLayout.CENTER);
        stacked.add(filaInferior, BorderLayout.SOUTH);

        wrapper.add(stacked, BorderLayout.CENTER);
        wrapper.add(extras, BorderLayout.SOUTH);
        return wrapper;
    }

    private void validarAcceso() {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, usuario != null && usuario.esControlEstudios(),
                "Solo control de estudios puede asignar docentes.");
    }

    private void recargarContexto() {
        cargarMaterias();
        cargarDocentesGuia();
        cargarDocentesFiltrados();
        refrescarDetalleSeccion(false);
    }

    private void cargarDocentesGuia() {
        List<Docente> docentes = AsignacionDocenteController.docentesAsignables();
        cbDocenteGuia.setModel(new DefaultComboBoxModel<>(docentes.toArray(Docente[]::new)));
        cbDocenteGuia.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getNombre() + " | " + value.getUsuario()));
    }

    private void cargarDocentesFiltrados() {
        String anio = cbAnio.getSelectedItem().toString();
        String seccion = cbSeccion.getSelectedItem().toString();
        String materia = cbMateria.getSelectedItem() == null ? "Castellano" : cbMateria.getSelectedItem().toString();
        List<Docente> docentes = AsignacionDocenteController.docentesPorMateriaDisponible(materia, anio, seccion);
        cbDocente.setModel(new DefaultComboBoxModel<>(docentes.toArray(Docente[]::new)));
        cbDocente.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getNombre() + " | " + value.getUsuario()));
        actualizarDisponibilidad();
    }

    private void actualizarDisponibilidad() {
        Docente docente = (Docente) cbDocente.getSelectedItem();
        if (docente == null) {
            lblDisponibilidad.setText("Disponibilidad: sin docentes para ese filtro");
            return;
        }
        int carga = AsignacionDocenteController.cargaActualCursos(docente.getId());
        int max = AsignacionDocenteController.capacidadMaximaCursos();
        Set<String> permitidas = AsignacionDocenteController.materiasPermitidas(docente.getId());
        lblDisponibilidad.setText("Disponibilidad: " + carga + "/" + max + " | Materias: " + String.join(", ", permitidas));
    }

    private void cargarMaterias() {
        String anio = cbAnio.getSelectedItem().toString();
        List<String> materias = AsignacionDocenteController.materiasParaAnio(anio);
        cbMateria.setModel(new DefaultComboBoxModel<>(materias.toArray(String[]::new)));
    }

    private void asignarMateria() {
        Docente docente = (Docente) cbDocente.getSelectedItem();
        if (docente == null) {
            return;
        }
        try {
            AsignacionDocenteController.asignarMateria(
                    docente.getId(),
                    cbAnio.getSelectedItem().toString(),
                    cbSeccion.getSelectedItem().toString(),
                    cbMateria.getSelectedItem().toString(),
                    cbDiaSemana.getSelectedItem().toString(),
                    cbHoraInicio.getSelectedItem().toString(),
                    cbHoraFin.getSelectedItem().toString());
            refrescarTabla();
            refrescarDetalleSeccion(false);
            cargarDocentesFiltrados();
            JOptionPane.showMessageDialog(this, "Materia asignada correctamente.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void asignarGuia() {
        Docente docente = (Docente) cbDocenteGuia.getSelectedItem();
        if (docente == null) {
            return;
        }
        try {
            AsignacionDocenteController.asignarProfesorGuia(
                    cbAnio.getSelectedItem().toString(),
                    cbSeccion.getSelectedItem().toString(),
                    docente.getId());
            refrescarDetalleSeccion(false);
            JOptionPane.showMessageDialog(this, "Profesor guía asignado correctamente.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void actualizarLapsoActivo() {
        try {
            LapsoController.setLapsoActivo((Integer) cbLapsoActivo.getSelectedItem());
            JOptionPane.showMessageDialog(this, "Lapso activo actualizado.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void configurarRangoLapso() {
        try {
            LapsoController.configurarRangoLapso(
                    controlador.AnioEscolarController.getAnioActivo(),
                    (Integer) cbLapsoActivo.getSelectedItem(),
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format((Date) spLapsoInicio.getValue()),
                    new java.text.SimpleDateFormat("yyyy-MM-dd").format((Date) spLapsoFin.getValue()));
            JOptionPane.showMessageDialog(this, "Rango del lapso guardado.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void cargarOpcionesHorario() {
        java.util.List<String> horas = new java.util.ArrayList<>();
        java.time.LocalTime t = java.time.LocalTime.of(7, 30);
        java.time.LocalTime fin = java.time.LocalTime.of(16, 0);
        while (!t.isAfter(fin)) {
            horas.add(t.toString());
            t = t.plusMinutes(30);
        }
        cbHoraInicio.setModel(new DefaultComboBoxModel<>(horas.toArray(String[]::new)));
        cbHoraFin.setModel(new DefaultComboBoxModel<>(horas.toArray(String[]::new)));
        cbHoraInicio.setSelectedItem("07:30");
        cbHoraFin.setSelectedItem("09:00");
    }

    private void regenerarDemo() {
        DemoDataController.regenerarDemoCompleto();
        recargarContexto();
        refrescarTabla();
        JOptionPane.showMessageDialog(this, "Datos demo regenerados correctamente.");
    }

    private void vaciarDemo() {
        int opt = JOptionPane.showConfirmDialog(this,
                "Esto eliminará estudiantes/asignaciones/notas/asistencia demo en memoria. ¿Desea continuar?",
                "Confirmar vaciado", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) {
            return;
        }
        DemoDataController.limpiarDemoCompleto();
        recargarContexto();
        refrescarTabla();
        JOptionPane.showMessageDialog(this, "Datos demo vaciados correctamente.");
    }

    private void refrescarTabla() {
        model.setRowCount(0);
        for (AsignacionDocente a : AsignacionDocenteController.listar()) {
            Docente docente = DocenteController.buscarPorId(a.getIdDocente());
            model.addRow(new Object[]{
                docente == null ? a.getIdDocente() : docente.getNombre(),
                a.getAnioEstudio(),
                a.getSeccion(),
                a.getMateria(),
                a.getDiaSemana() + " " + a.getHoraInicio() + "-" + a.getHoraFin()
            });
        }
    }

    private void refrescarDetalleSeccion(boolean notificarGuiaPendiente) {
        String anio = cbAnio.getSelectedItem().toString();
        String seccion = cbSeccion.getSelectedItem().toString();

        List<Estudiante> estudiantes = EstudianteController.listarPorAnioYSeccion(anio, seccion);
        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorAnioSeccion(anio, seccion);
        Docente guia = AsignacionDocenteController.buscarProfesorGuia(anio, seccion);
        if (guia == null && notificarGuiaPendiente) {
            int option = JOptionPane.showConfirmDialog(this,
                    "La sección " + anio + " " + seccion + " no tiene profesor guía. ¿Desea asignarlo ahora?",
                    "Profesor guía pendiente",
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                cbDocenteGuia.requestFocusInWindow();
            }
        }

        lblResumenSeccion.setText("Detalle de sección: " + anio + " - " + seccion
                + " | Profesor guía: " + (guia == null ? "SIN ASIGNAR" : guia.getNombre())
                + " | Estudiantes: " + estudiantes.size());

        modelEstudiantes.setRowCount(0);
        for (Estudiante e : estudiantes) {
            modelEstudiantes.addRow(new Object[]{e.getCedula(), e.getNombreCompleto()});
        }

        modelMateriasSeccion.setRowCount(0);
        for (String materia : AsignacionDocenteController.materiasParaAnio(anio)) {
            List<String> docentesMateria = asignaciones.stream()
                    .filter(a -> a.getMateria().equalsIgnoreCase(materia))
                    .map(a -> DocenteController.buscarPorId(a.getIdDocente()))
                    .filter(d -> d != null)
                    .map(Docente::getNombre)
                    .distinct()
                    .collect(Collectors.toList());
            modelMateriasSeccion.addRow(new Object[]{
                materia + " (" + asignaciones.stream()
                        .filter(a -> a.getMateria().equalsIgnoreCase(materia))
                        .map(a -> a.getDiaSemana() + " " + a.getHoraInicio() + "-" + a.getHoraFin())
                        .findFirst().orElse("Sin horario") + ")",
                docentesMateria.isEmpty() ? "SIN ASIGNAR" : String.join(", ", docentesMateria)
            });
        }
    }
}
