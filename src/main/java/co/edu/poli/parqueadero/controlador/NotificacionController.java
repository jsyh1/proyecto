package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.NotificacionResponse;
import co.edu.poli.parqueadero.servicios.NotificacionService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios/me/notificaciones")
public class NotificacionController {

    private final NotificacionService servicio;

    public NotificacionController(NotificacionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<NotificacionResponse> listar(Authentication autenticacion) {
        return servicio.listar(autenticacion.getName());
    }
}
