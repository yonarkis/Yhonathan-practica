package Vista;

import controlador.EstudianteController;
import controlador.NotaController;
import controlador.RepresentanteController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import modelo.Estudiante;

public class ConsultarPromedios extends JFrame {

    private final JTextField txtCedula = new JTextField(12);
    private final JTextArea txtResultado = new JTextArea();

    public ConsultarPromedios() {
        setTitle("Consulta académica");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        ((AbstractDocument) txtCedula.getDocument()).setDocumentFilter(new NumericDocumentFilter(9));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(UiStyles.topBar("Consulta académica integral"), BorderLayout.NORTH);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT));
        UiStyles.card(search);
        search.add(new JLabel("Cédula:"));
        search.add(txtCedula);
        JButton btn = UiStyles.primaryButton("Consultar expediente");
        btn.addActionListener(e -> consultar());
        search.add(btn);
        north.add(search, BorderLayout.CENTER);

        txtResultado.setEditable(false);
        txtResultado.setLineWrap(true);
        txtResultado.setWrapStyleWord(true);
        txtResultado.setFont(new java.awt.Font("Consolas", java.awt.Font.PLAIN, 14));

        root.add(north, BorderLayout.NORTH);
        root.add(new JScrollPane(txtResultado), BorderLayout.CENTER);
        setContentPane(root);
    }

    private void consultar() {
        try {
            Estudiante e = EstudianteController.buscarPorCedula(txtCedula.getText());
            if (e == null) {
                txtResultado.setText("No existe estudiante con esa cédula.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("CONSULTA ACADÉMICA DEL ESTUDIANTE\n");
            sb.append("────────────────────────────────────────\n");
            sb.append("Nombre completo: ").append(e.getNombreCompleto()).append("\n");
            sb.append("Cédula: ").append(e.getCedula()).append("\n");
            sb.append("Grado: ").append(e.getGrado()).append("\n");
            sb.append("Sección: ").append(e.getSeccion()).append("\n");
            modelo.Representante r = RepresentanteController.buscarPorCedulaEstudiante(e.getCedula());
            if (r != null) {
                sb.append("Madre o padre: ").append(r.getNombreMadre()).append(" ").append(r.getApellidoMadre())
                        .append(" | C.I. ").append(r.getCedulaMadre()).append("\n");
                if (r.getNombreRepresentante() != null && !r.getNombreRepresentante().isBlank()) {
                    sb.append("Representante: ").append(r.getNombreRepresentante()).append(" ")
                            .append(r.getApellidoRepresentante()).append(" | C.I. ")
                            .append(r.getCedulaRepresentante()).append(" | Parentesco: ")
                            .append(r.getParentescoRepresentante()).append("\n");
                }
            }
            sb.append("\n");

            Map<String, String> reporte = NotaController.reportePromedioEstadoPorMateria(e.getCedula());
            if (reporte.isEmpty()) {
                sb.append("No hay notas cargadas en el boletín informativo.");
            } else {
                sb.append("BOLETÍN INFORMATIVO\n");
                sb.append("────────────────────────────────────────\n");
                reporte.forEach((materia, info) -> sb.append("- ").append(materia).append(" -> ").append(info).append("\n"));
            }

            txtResultado.setText(sb.toString());
            txtResultado.setCaretPosition(0);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}
