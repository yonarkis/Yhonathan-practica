package Vista;

import controlador.AsignacionDocenteController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import modelo.AsignacionDocente;
import modelo.Docente;

public class PerfilDocente extends JFrame {

    private static final String[] DIAS = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes"};

    public PerfilDocente(Docente docente) {
        setTitle("Perfil docente");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        root.add(UiStyles.topBar("Perfil docente y carga académica"), BorderLayout.NORTH);

        JPanel resumen = new JPanel(new GridLayout(0, 1, 4, 4));
        UiStyles.card(resumen);
        resumen.add(UiStyles.titleLabel(docente.getNombre()));
        resumen.add(new JLabel("Especializaciones: " + String.join(", ",
                AsignacionDocenteController.materiasPermitidas(docente.getId()))));
        resumen.add(new JLabel("Títulos: " + obtenerTitulos(docente)));
        resumen.add(new JLabel("Honorarios: " + obtenerHonorarios(docente)));

        JPanel fotoCard = new JPanel(new FlowLayout(FlowLayout.LEFT));
        UiStyles.card(fotoCard);
        JLabel avatar = new JLabel(docente.getNombre().substring(0, 1));
        avatar.setOpaque(true);
        avatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        avatar.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        avatar.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 42));
        avatar.setForeground(java.awt.Color.WHITE);
        avatar.setBackground(UiStyles.COLOR_PRIMARY_DARK);
        avatar.setPreferredSize(new java.awt.Dimension(96, 96));
        fotoCard.add(avatar);
        fotoCard.add(UiStyles.subtitleLabel("Foto de perfil institucional"));

        JPanel indicadores = new JPanel(new GridLayout(1, 3, 8, 8));
        UiStyles.card(indicadores);
        indicadores.add(crearIndicador("Materias",
                String.valueOf(AsignacionDocenteController.materiasPermitidas(docente.getId()).size())));
        indicadores.add(crearIndicador("Cursos",
                String.valueOf(AsignacionDocenteController.listarPorDocente(docente.getId()).size())));
        indicadores.add(crearIndicador("Secciones guía",
                String.valueOf(AsignacionDocenteController.seccionesGuiadasPorDocente(docente.getId()).size())));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Resumen", new JScrollPane(areaDetalle(construirResumenPerfil(docente))));
        tabs.addTab("Cursos", new JScrollPane(areaDetalle(construirCursos(docente))));
        tabs.addTab("Horario", new JScrollPane(areaDetalle(construirHorario(docente))));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setOpaque(false);
        top.add(fotoCard, BorderLayout.WEST);
        top.add(resumen, BorderLayout.CENTER);
        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setOpaque(false);
        contenido.add(indicadores, BorderLayout.NORTH);
        contenido.add(tabs, BorderLayout.CENTER);

        center.add(top, BorderLayout.NORTH);
        center.add(contenido, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JTextArea areaDetalle(String contenido) {
        JTextArea txt = new JTextArea(contenido);
        txt.setEditable(false);
        txt.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 14));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setForeground(new java.awt.Color(35, 45, 55));
        UiStyles.card(txt);
        txt.setCaretPosition(0);
        return txt;
    }

    private String construirResumenPerfil(Docente docente) {
        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(docente.getId());
        Set<String> materias = asignaciones.stream()
                .map(AsignacionDocente::getMateria)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        StringBuilder sb = new StringBuilder();
        sb.append("MATERIAS ASIGNADAS\n");
        if (materias.isEmpty()) {
            sb.append("- Sin materias asignadas en el año activo.\n");
        } else {
            for (String materia : materias) {
                sb.append("- ").append(materia).append('\n');
            }
        }

        sb.append("\nCURSOS ASIGNADOS\n");
        if (asignaciones.isEmpty()) {
            sb.append("- Sin cursos asignados.\n");
        } else {
            for (AsignacionDocente asignacion : asignaciones) {
                sb.append("- ")
                        .append(asignacion.getAnioEstudio())
                        .append(" | Sección ")
                        .append(asignacion.getSeccion())
                        .append(" | ")
                        .append(asignacion.getMateria())
                        .append('\n');
            }
        }

        List<String> seccionesGuia = AsignacionDocenteController.seccionesGuiadasPorDocente(docente.getId());
        sb.append("\nPROFESOR GUÍA\n");
        if (seccionesGuia.isEmpty()) {
            sb.append("- Sin sección guía asignada.\n");
        } else {
            for (String seccion : seccionesGuia) {
                sb.append("- ").append(seccion.replace("#", " | Sección ")).append('\n');
            }
        }
        return sb.toString();
    }

    private String construirCursos(Docente docente) {
        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(docente.getId());
        StringBuilder sb = new StringBuilder("CURSOS ASIGNADOS\n");
        if (asignaciones.isEmpty()) {
            sb.append("- Sin cursos asignados.\n");
            return sb.toString();
        }
        for (AsignacionDocente a : asignaciones) {
            sb.append("- ").append(a.getAnioEstudio()).append(" | Sección ").append(a.getSeccion())
                    .append(" | ").append(a.getMateria()).append(" | ")
                    .append(a.getDiaSemana()).append(" ").append(a.getHoraInicio()).append("-").append(a.getHoraFin())
                    .append('\n');
        }
        return sb.toString();
    }

    private String construirHorario(Docente docente) {
        List<AsignacionDocente> asignaciones = AsignacionDocenteController.listarPorDocente(docente.getId());
        StringBuilder sb = new StringBuilder("\nHORARIO SEMANAL\n");
        Map<String, List<String>> horarios = generarHorario(asignaciones);
        for (String dia : DIAS) {
            sb.append(dia).append('\n');
            List<String> bloques = horarios.getOrDefault(dia, List.of());
            if (bloques.isEmpty()) {
                sb.append("  - Sin bloque asignado.\n");
                continue;
            }
            for (String b : bloques) {
                sb.append("  - ").append(b).append('\n');
            }
        }
        return sb.toString();
    }

    private Map<String, List<String>> generarHorario(List<AsignacionDocente> asignaciones) {
        Map<String, List<String>> horario = new LinkedHashMap<>();
        for (String dia : DIAS) {
            horario.put(dia, new java.util.ArrayList<>());
        }
        for (AsignacionDocente asignacion : asignaciones) {
            String dia = asignacion.getDiaSemana() == null || asignacion.getDiaSemana().isBlank()
                    ? DIAS[0]
                    : asignacion.getDiaSemana();
            horario.computeIfAbsent(dia, key -> new java.util.ArrayList<>()).add(
                    asignacion.getHoraInicio() + "-" + asignacion.getHoraFin() + " | " + asignacion.getAnioEstudio()
                    + "-" + asignacion.getSeccion() + " | " + asignacion.getMateria());
        }
        return horario;
    }

    private JPanel crearIndicador(String titulo, String valor) {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setOpaque(false);
        JLabel lblTitulo = new JLabel(titulo, javax.swing.SwingConstants.CENTER);
        lblTitulo.setForeground(new java.awt.Color(90, 100, 110));
        JLabel lblValor = new JLabel(valor, javax.swing.SwingConstants.CENTER);
        lblValor.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        lblValor.setForeground(UiStyles.COLOR_PRIMARY_DARK);
        panel.add(lblTitulo);
        panel.add(lblValor);
        return panel;
    }

    private String obtenerTitulos(Docente docente) {
        return switch (docente.getId()) {
            case 1 -> "Lic. en Educación Matemática";
            case 2 -> "Prof. en Lengua y Literatura";
            case 3 -> "Lic. en Ciencias Pedagógicas";
            case 4 -> "MgSc. en Gestión Educativa";
            default -> "Licenciatura en Educación";
        };
    }

    private String obtenerHonorarios(Docente docente) {
        return switch (docente.getId()) {
            case 1 -> "Docente Nivel III - 22 horas cátedra";
            case 2 -> "Docente Nivel II - 18 horas cátedra";
            case 3 -> "Docente Nivel III - 20 horas cátedra";
            case 4 -> "Coordinación Académica - 24 horas";
            default -> "Escala institucional vigente";
        };
    }
}
