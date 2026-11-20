package co.edu.poli.parqueadero.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.poli.parqueadero.servicios.DetalleHistorialSesionResponse;
import co.edu.poli.parqueadero.servicios.HistorialSesionResponse;
import co.edu.poli.parqueadero.servicios.HistorialSesionService;
import co.edu.poli.parqueadero.seguridad.AutorizacionUsuarioService;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/usuarios/{idUsuario}/sesiones")
public class HistorialSesionController {

    private final HistorialSesionService servicio;
    private final AutorizacionUsuarioService autorizacion;

    public HistorialSesionController(
            HistorialSesionService servicio, AutorizacionUsuarioService autorizacion) {
        this.servicio = servicio;
        this.autorizacion = autorizacion;
    }

    @GetMapping
    public List<HistorialSesionResponse> listar(
            @PathVariable Integer idUsuario, Authentication autenticacion) {
        autorizacion.validarAcceso(idUsuario, autenticacion);
        return servicio.listar(idUsuario);
    }

    @GetMapping("/{idSesion}")
    public DetalleHistorialSesionResponse consultar(
            @PathVariable Integer idUsuario,
            @PathVariable Integer idSesion,
            Authentication autenticacion) {
        autorizacion.validarAcceso(idUsuario, autenticacion);
        return servicio.consultar(idUsuario, idSesion);
    }
}
