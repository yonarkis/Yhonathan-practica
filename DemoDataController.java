package controlador;

public final class DemoDataController {

    private DemoDataController() {
    }

    public static void regenerarDemoCompleto() {
        limpiarTodo();
        EstudianteController.regenerarDatosDemo();
        AsignacionDocenteController.reinicializarCoberturaDemo();
        AuditoriaController.registrar("DEMO_REGENERADA", "-", "Se regeneraron estudiantes demo y se limpiaron asignaciones docentes");
    }

    public static void limpiarDemoCompleto() {
        limpiarTodo();
        AuditoriaController.registrar("DEMO_VACIADA", "-", "Se vaciaron datos demo en memoria");
    }

    private static void limpiarTodo() {
        NotaController.limpiarMemoria();
        AsistenciaController.limpiarMemoria();
        ObservacionDocenteController.limpiarMemoria();
        InscripcionController.limpiarMemoria();
        EstudianteController.limpiarDatosMemoria();
        AsignacionDocenteController.limpiarAsignacionesMemoria();
    }
}
