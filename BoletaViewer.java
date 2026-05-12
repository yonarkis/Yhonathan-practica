package Vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JLabel;

public class BoletaViewer extends JFrame {

    public BoletaViewer(String contenido) {
        setTitle("Boleta de calificaciones");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);
        setExtendedState(JFrame.NORMAL);
        setSize(900, 620);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(6, 6));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        root.add(UiStyles.topBar("Boleta de calificaciones"), BorderLayout.NORTH);

        JLabel lbl = UiStyles.subtitleLabel("Boletín informativo de rendimiento académico");
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lbl, BorderLayout.WEST);

        JTextArea area = new JTextArea(contenido);
        area.setEditable(false);
        area.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setMargin(new java.awt.Insets(10, 12, 10, 12));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        JButton btnCerrar = UiStyles.primaryButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        acciones.add(btnCerrar);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(header, BorderLayout.NORTH);
        center.add(new JScrollPane(area), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        root.add(acciones, BorderLayout.SOUTH);
        setContentPane(root);
    }
}
