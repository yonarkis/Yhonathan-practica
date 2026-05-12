package Vista;

import controlador.AccesoController;
import controlador.AsignacionDocenteController;
import controlador.EstudianteController;
import controlador.LapsoController;
import controlador.NotaController;
import controlador.SessionContext;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import modelo.AsignacionDocente;
import modelo.Docente;
import modelo.Estudiante;

public class CargaMasivaNotasDocente extends JFrame {

    private final JComboBox<AsignacionDocente> cbAsignacion = new JComboBox<>();
    private final JComboBox<Integer> cbLapso = new JComboBox<>(new Integer[]{1, 2, 3});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Cédula", "Estudiante", "Nota 1", "Nota 2", "Nota 3", "Nota 4", "Nota 5"}, 0);

    public CargaMasivaNotasDocente() {
        setTitle("Carga masiva de notas");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        validarAcceso();

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(UiStyles.topBar("Carga masiva de notas por grupo"), BorderLayout.NORTH);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        UiStyles.card(filtros);
        filtros.add(new JLabel("Grupo:"));
        filtros.add(cbAsignacion);
        filtros.add(new JLabel("Lapso:"));
        filtros.add(cbLapso);
        JButton btnCargar = UiStyles.primaryButton("Cargar grupo");
        btnCargar.addActionListener(e -> cargarGrupo());
        filtros.add(btnCargar);
        JButton btnGuardar = UiStyles.primaryButton("Guardar grupo");
        btnGuardar.addActionListener(e -> guardarGrupo());
        filtros.add(btnGuardar);

        JTable tabla = new JTable(model);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        configurarEditorNotas(tabla);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(filtros, BorderLayout.NORTH);
        center.add(new JScrollPane(tabla), BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);

        cbLapso.setSelectedItem(LapsoController.getLapsoActivo());
        cbLapso.setEnabled(false);
        cargarAsignaciones();
    }

    private void validarAcceso() {
        Docente usuario = SessionContext.getUsuarioActual();
        AccesoController.validar(usuario, usuario != null && usuario.esDocente(),
                "Solo un docente puede abrir la carga masiva de notas.");
    }

    private void cargarAsignaciones() {
        Docente usuario = SessionContext.getUsuarioActual();
        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(usuario.getId());
        cbAsignacion.setModel(new DefaultComboBoxModel<>(asignaciones.toArray(AsignacionDocente[]::new)));
        cbAsignacion.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value == null ? "-" : value.getEtiqueta()));
        cargarGrupo();
    }

    private void cargarGrupo() {
        model.setRowCount(0);
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        if (asignacion == null) {
            return;
        }
        for (Estudiante estudiante : EstudianteController.listarPorAnioYSeccion(asignacion.getAnioEstudio(), asignacion.getSeccion())) {
            model.addRow(new Object[]{estudiante.getCedula(), estudiante.getNombreCompleto(), "", "", "", "", ""});
        }
    }


    private void configurarEditorNotas(JTable tabla) {
        for (int col = 2; col <= 6; col++) {
            JTextField field = new JTextField();
            ((AbstractDocument) field.getDocument()).setDocumentFilter(new DecimalDocumentFilter(4, 20.0));
            tabla.getColumnModel().getColumn(col).setCellEditor(new DefaultCellEditor(field));
        }
    }

    private void guardarGrupo() {
        AsignacionDocente asignacion = (AsignacionDocente) cbAsignacion.getSelectedItem();
        if (asignacion == null) {
            JOptionPane.showMessageDialog(this, "No hay asignación seleccionada.");
            return;
        }
        try {
            Map<String, List<Double>> notasPorCedula = new LinkedHashMap<>();
            for (int row = 0; row < model.getRowCount(); row++) {
                String cedula = String.valueOf(model.getValueAt(row, 0));
                List<Double> notas = new ArrayList<>();
                for (int col = 2; col <= 6; col++) {
                    Object raw = model.getValueAt(row, col);
                    if (raw == null) {
                        continue;
                    }
                    String valor = raw.toString().trim();
                    if (valor.isBlank()) {
                        continue;
                    }
                    if (!valor.matches("\\d{1,2}(\\.\\d)?")) {
                        throw new IllegalArgumentException("Formato inválido: use valores entre 0.1 y 20 (máximo 1 decimal).");
                    }
                    double numero = Double.parseDouble(valor);
                    if (numero < 0.1 || numero > 20) {
                        throw new IllegalArgumentException("Todas las notas deben estar entre 0.1 y 20.");
                    }
                    notas.add(numero);
                }
                if (!notas.isEmpty()) {
                    notasPorCedula.put(cedula, notas);
                }
            }
            NotaController.guardarNotasPorGrupo(asignacion.getMateria(), (Integer) cbLapso.getSelectedItem(), notasPorCedula);
            JOptionPane.showMessageDialog(this, "Grupo guardado correctamente.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Las notas deben ser numéricas.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
