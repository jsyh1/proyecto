package co.edu.poli.parqueadero.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.poli.parqueadero.servicios.DetalleHistorialSesionResponse;
import co.edu.poli.parqueadero.servicios.HistorialSesionResponse;
import co.edu.poli.parqueadero.servicios.HistorialSesionService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/usuarios/{idUsuario}/sesiones")
public class HistorialSesionController {

    private final HistorialSesionService servicio;

    public HistorialSesionController(HistorialSesionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<HistorialSesionResponse> listar(
            Authentication autenticacion,
            @PathVariable Integer idUsuario) {
        return servicio.listar(idUsuario, autenticacion.getName());
    }

    @GetMapping("/{idSesion}")
    public DetalleHistorialSesionResponse consultar(
            Authentication autenticacion,
            @PathVariable Integer idUsuario,
            @PathVariable Integer idSesion) {
        return servicio.consultar(idUsuario, idSesion, autenticacion.getName());
    }
}
