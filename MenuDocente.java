package Vista;

import controlador.AccesoController;
import controlador.AnioEscolarController;
import controlador.AuditoriaController;
import controlador.EstudianteController;
import controlador.InscripcionController;
import controlador.NotaController;
import controlador.SessionContext;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import modelo.Docente;

public class MenuDocente extends JFrame {

    private final Docente docente;

    public MenuDocente(Docente docente) {
        this.docente = docente;
        setTitle("Sistema de Control Escolar - Panel " + docente.getRol());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        UiStyles.applyWindowDefaults(this);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBackground(UiStyles.COLOR_BG);
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));

        root.add(buildTopToolbar(), BorderLayout.NORTH);
        root.add(buildCenterContent(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JToolBar buildTopToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(java.awt.Color.WHITE);

        if (AccesoController.puedeGestionarEstudiantes(docente)) {
            JButton btnRegistro = new JButton("Expedientes");
            btnRegistro.addActionListener(e -> new RegistrarEstudiantes(docente).setVisible(true));
            toolBar.add(btnRegistro);
        }

        if (AccesoController.puedeGestionarNotas(docente)) {
            JButton btnNotas = new JButton("Notas");
            btnNotas.addActionListener(e -> new GestionNotas().setVisible(true));
            toolBar.add(btnNotas);
        }

        if (docente.esControlEstudios()) {
            JButton btnAsignaciones = new JButton("Asignaciones");
            btnAsignaciones.addActionListener(e -> new AsignacionDocentesControlEstudios().setVisible(true));
            toolBar.add(btnAsignaciones);
        }

        if (docente.esDocente()) {
            JButton btnPerfil = new JButton("Mi perfil");
            btnPerfil.addActionListener(e -> new PerfilDocente(docente).setVisible(true));
            toolBar.add(btnPerfil);

            JButton btnAsistencia = new JButton("Asistencia");
            btnAsistencia.addActionListener(e -> new ControlAsistencia().setVisible(true));
            toolBar.add(btnAsistencia);

            JButton btnCargaMasiva = new JButton("Carga grupal");
            btnCargaMasiva.addActionListener(e -> new CargaMasivaNotasDocente().setVisible(true));
            toolBar.add(btnCargaMasiva);

            JButton btnResumen = new JButton("Resumen asistencia");
            btnResumen.addActionListener(e -> new ResumenAsistenciaDocente().setVisible(true));
            toolBar.add(btnResumen);

            JButton btnObservaciones = new JButton("Observaciones");
            btnObservaciones.addActionListener(e -> new ObservacionesDocente().setVisible(true));
            toolBar.add(btnObservaciones);

            JButton btnHistorial = new JButton("Historial");
            btnHistorial.addActionListener(e -> new HistorialDocente().setVisible(true));
            toolBar.add(btnHistorial);
        }

        if (AccesoController.puedeVerBoletines(docente)) {
            JButton btnBoletas = new JButton("Boletines");
            btnBoletas.addActionListener(e -> generarBoleta());
            toolBar.add(btnBoletas);
        }

        if (AccesoController.puedeVerAuditoria(docente)) {
            JButton btnAuditoria = new JButton("Auditoría");
            btnAuditoria.addActionListener(e -> verAuditoria());
            toolBar.add(btnAuditoria);
        }

        if (AccesoController.puedeCerrarPeriodos(docente)) {
            JButton btnCierre = new JButton("Cierre año");
            btnCierre.addActionListener(e -> cerrarAnioActivo());
            toolBar.add(btnCierre);
        }

        toolBar.addSeparator();
        JButton btnCerrar = new JButton("Cerrar sesión");
        btnCerrar.addActionListener(e -> {
            SessionContext.clear();
            new LoginDocente().setVisible(true);
            dispose();
        });
        toolBar.add(btnCerrar);
        return toolBar;
    }

    private JPanel buildCenterContent() {
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setBackground(UiStyles.COLOR_BG);

        JPanel welcome = new JPanel(new GridLayout(2, 1));
        UiStyles.card(welcome);
        welcome.add(UiStyles.titleLabel(tituloSegunRol()));
        welcome.add(UiStyles.subtitleLabel("Usuario: " + docente.getNombre()
                + " | Rol: " + docente.getRol()
                + " | Año activo: " + AnioEscolarController.getAnioActivo()));

        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setBackground(UiStyles.COLOR_BG);

        if (docente.esDocente()) {
            grid.add(buildActionCard("Mi perfil docente",
                    "Revisar datos de usuario, materias, cursos y horario semanal asignado.",
                    () -> new PerfilDocente(docente).setVisible(true)));
            grid.add(buildActionCard("Carga de calificaciones",
                    "Registrar notas del lapso actual para grupos asignados.",
                    () -> new GestionNotas().setVisible(true)));
            grid.add(buildActionCard("Carga masiva por grupo",
                    "Guardar en lote hasta cinco evaluaciones por estudiante dentro del lapso activo.",
                    () -> new CargaMasivaNotasDocente().setVisible(true)));
            grid.add(buildActionCard("Control de asistencia",
                    "Registrar presente, ausente o justificado por estudiante.",
                    () -> new ControlAsistencia().setVisible(true)));
            grid.add(buildActionCard("Resumen de asistencia",
                    "Consultar acumulados de presentes, ausentes y justificados por grupo.",
                    () -> new ResumenAsistenciaDocente().setVisible(true)));
            grid.add(buildActionCard("Observaciones del docente",
                    "Documentar novedades académicas, de conducta, asistencia o seguimiento.",
                    () -> new ObservacionesDocente().setVisible(true)));
            grid.add(buildActionCard("Historial del docente",
                    "Revisar observaciones registradas y alertas académicas por grupo.",
                    () -> new HistorialDocente().setVisible(true)));
            grid.add(buildActionCard("Consulta académica",
                    "Consultar materias, promedios y estados por estudiante.",
                    () -> new ConsultarPromedios().setVisible(true)));
            grid.add(buildActionCard("Listado por sección",
                    "Ver estudiantes inscritos en una sección del año activo.",
                    () -> new ListadoEstudiantesMateria().setVisible(true)));
        }

        if (docente.esControlEstudios()) {
            grid.add(buildActionCard("Registro histórico",
                    "Crear y mantener expedientes, inscripción y representantes.",
                    () -> new RegistrarEstudiantes(docente).setVisible(true)));
            grid.add(buildActionCard("Listado por año/sección",
                    "Consultar estudiantes por año y sección (vista global para control de estudios).",
                    () -> new ListadoEstudiantesMateria().setVisible(true)));
            grid.add(buildActionCard("Corrección de notas",
                    "Centralizar correcciones con trazabilidad de auditoría.",
                    () -> new GestionNotas().setVisible(true)));
            grid.add(buildActionCard("Boletines informativos",
                    "Emitir reportes de notas por lapso para representantes.",
                    this::generarBoleta));
            grid.add(buildActionCard("Constancias",
                    "Reservado para constancias de estudio, conducta e inscripción.",
                    () -> mostrarMensaje("Constancias", "Próxima fase: emisión de constancias y certificaciones.")));
            grid.add(buildActionCard("Materias pendientes",
                    "Reservado para revisión, reparación y materias arrastradas.",
                    () -> mostrarMensaje("Pendientes", "Próxima fase: pendientes, revisión y reparación.")));
            grid.add(buildActionCard("Auditoría de notas",
                    "Ver historial de cambios sensibles realizados en notas.",
                    this::verAuditoria));
            grid.add(buildActionCard("Asignación docente",
                    "Asignar materias/profesor guía y definir el lapso activo del plantel.",
                    () -> new AsignacionDocentesControlEstudios().setVisible(true)));
        }

        if (docente.esDirector()) {
            grid.add(buildActionCard("Dashboard institucional",
                    "Resumen de matrícula, rendimiento y alertas del plantel.",
                    this::mostrarDashboard));
            grid.add(buildActionCard("Top 10 y rendimiento",
                    "Indicadores académicos por materia y sección.",
                    this::consultarTop10));
            grid.add(buildActionCard("Cierre académico",
                    "Cerrar el año activo para bloquear modificaciones.",
                    this::cerrarAnioActivo));
            grid.add(buildActionCard("Auditoría",
                    "Revisar cambios sobre notas y operaciones sensibles.",
                    this::verAuditoria));
            grid.add(buildActionCard("Carga horaria",
                    "Reservado para asignar profesor, sección y horario.",
                    () -> mostrarMensaje("Carga horaria", "Próxima fase: asignación docente-sección-horario.")));
            grid.add(buildActionCard("Boletines y actas",
                    "Consultar boletas y reportes finales para firma.",
                    this::generarBoleta));
        }

        if (docente.esAdmin()) {
            grid.add(buildActionCard("Administración global",
                    "Control total de módulos académicos, usuarios y configuración institucional.",
                    this::mostrarDashboard));
            grid.add(buildActionCard("Control de estudios",
                    "Acceso rápido a registro histórico, asignaciones y gestión de notas.",
                    () -> new RegistrarEstudiantes(docente).setVisible(true)));
            grid.add(buildActionCard("Configuración DEA",
                    "Reservado para parámetros administrativos y códigos DEA del plantel.",
                    () -> mostrarMensaje("Configuración DEA", "Próxima fase: catálogo y validación de códigos DEA.")));
            grid.add(buildActionCard("Auditoría integral",
                    "Revisión de operaciones sensibles y trazabilidad completa del sistema.",
                    this::verAuditoria));
        }

        center.add(welcome, BorderLayout.NORTH);
        center.add(new JScrollPane(grid), BorderLayout.CENTER);
        return center;
    }

    private JPanel buildActionCard(String titulo, String detalle, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        UiStyles.card(card);
        card.add(new JLabel("<html><b>" + titulo + "</b><br/>" + detalle + "</html>"), BorderLayout.CENTER);
        JButton btn = UiStyles.primaryButton("Abrir");
        btn.addActionListener(e -> action.run());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(btn);
        card.add(btnPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildStatusBar() {
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT));
        status.setBackground(UiStyles.COLOR_PRIMARY_DARK);
        JLabel lbl = new JLabel("Usuario: " + docente.getUsuario()
                + " | Rol: " + docente.getRol()
                + " | Acceso en memoria RBAC");
        lbl.setForeground(java.awt.Color.WHITE);
        status.add(lbl);
        return status;
    }

    private String tituloSegunRol() {
        if (docente.esControlEstudios()) {
            return "Panel de Control de Estudios";
        }
        if (docente.esDirector()) {
            return "Panel de Dirección";
        }
        if (docente.esAdmin()) {
            return "Panel de Administración";
        }
        if (docente.esDocente()) {
            return "Panel Docente";
        }
        return "Panel del Sistema";
    }

    private void generarBoleta() {
        if (!AccesoController.puedeVerBoletines(docente)) {
            mostrarMensaje("Acceso denegado", "Este rol no puede emitir boletines.");
            return;
        }
        String cedula = JOptionPane.showInputDialog(this, "Cédula del estudiante:");
        if (cedula == null || cedula.isBlank()) {
            return;
        }
        try {
            new BoletaViewer(NotaController.generarBoletaPorCedula(cedula)).setVisible(true);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void consultarTop10() {
        String materia = (String) JOptionPane.showInputDialog(this, "Materia:", "Top 10",
                JOptionPane.QUESTION_MESSAGE, null, NotaController.MATERIAS.toArray(), NotaController.MATERIAS.get(0));
        String seccion = (String) JOptionPane.showInputDialog(this, "Sección:", "Top 10",
                JOptionPane.QUESTION_MESSAGE, null, EstudianteController.SECCIONES_VALIDAS.toArray(), "A");
        if (materia == null || seccion == null) {
            return;
        }
        List<String> top = NotaController.top10PorMateriaYSeccion(materia, seccion);
        JOptionPane.showMessageDialog(this, top.isEmpty() ? "No hay datos para mostrar." : String.join("\n", top));
    }

    private void verAuditoria() {
        List<String> lineas = AuditoriaController.listar().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        JTextArea area = new JTextArea(lineas.isEmpty() ? "Sin registros." : String.join("\n", lineas));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Auditoría", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarDashboard() {
        long estudiantes = EstudianteController.listar().size();
        long alertas = InscripcionController.ANIOS_ESTUDIO_VALIDOS.stream()
                .flatMap(anioEstudio -> EstudianteController.SECCIONES_VALIDAS.stream()
                        .flatMap(seccion -> NotaController.MATERIAS.stream()
                                .flatMap(materia -> NotaController.alertasAcademicasPorGrupo(anioEstudio, seccion, materia).stream())))
                .count();
        mostrarMensaje("Dashboard", "Estudiantes registrados: " + estudiantes + "\nAlertas académicas detectadas: " + alertas);
    }

    private void cerrarAnioActivo() {
        if (!AccesoController.puedeCerrarPeriodos(docente)) {
            mostrarMensaje("Acceso denegado", "Este rol no puede cerrar años escolares.");
            return;
        }
        try {
            AnioEscolarController.cerrarAnio(AnioEscolarController.getAnioActivo());
            mostrarMensaje("Cierre académico", "Año escolar cerrado correctamente.");
        } catch (IllegalStateException ex) {
            mostrarMensaje("Cierre académico", ex.getMessage());
        }
    }

    private void mostrarMensaje(String titulo, String detalle) {
        JOptionPane.showMessageDialog(this, detalle, titulo, JOptionPane.INFORMATION_MESSAGE);
    }
}
