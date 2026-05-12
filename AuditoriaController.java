package controlador;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import modelo.Docente;
import modelo.RegistroAuditoria;

public final class AuditoriaController {

    private static final List<RegistroAuditoria> registros = new ArrayList<>();
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AuditoriaController() {
    }

    public static void registrar(String accion, String detalleAntes, String detalleDespues) {
        Docente usuario = SessionContext.getUsuarioActual();
        String nombreUsuario = usuario == null ? "sistema" : usuario.getUsuario();
        String rol = usuario == null ? "sistema" : usuario.getRol();
        registros.add(new RegistroAuditoria(
                LocalDateTime.now().format(FORMATO),
                nombreUsuario,
                rol,
                accion,
                detalleAntes == null ? "-" : detalleAntes,
                detalleDespues == null ? "-" : detalleDespues
        ));
    }

    public static List<RegistroAuditoria> listar() {
        return Collections.unmodifiableList(registros);
    }

    public static List<RegistroAuditoria> listarPorUsuario(String usuario) {
        return registros.stream()
                .filter(r -> r.getUsuario().equalsIgnoreCase(usuario))
                .collect(Collectors.toList());
    }

    public static void limpiarMemoria() {
        registros.clear();
    }
}
