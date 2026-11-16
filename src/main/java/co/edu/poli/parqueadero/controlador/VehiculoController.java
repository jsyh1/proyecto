package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.modelo.Vehiculo;
import co.edu.poli.parqueadero.servicios.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import co.edu.poli.parqueadero.servicios.UsuarioService;

@RestController
@RequestMapping({"/vehiculos", "/api/vehiculos"})
public class VehiculoController {

    private final VehiculoService servicio;
    private final UsuarioService usuarioService;

    public VehiculoController(VehiculoService servicio, UsuarioService usuarioService) {
        this.servicio = servicio;
        this.usuarioService = usuarioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponse registrar(
            Authentication autenticacion,
            @Valid @RequestBody RegistrarVehiculoRequest solicitud) {
        Integer idUsuarioActual = usuarioService.buscarPorCorreoAutenticado(autenticacion.getName()).getId();
        if (!idUsuarioActual.equals(solicitud.idUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo puedes registrar vehículos para tu propia cuenta.");
        }
        Vehiculo vehiculo = servicio.registrar(
                solicitud.idUsuario(),
                solicitud.placa(),
                solicitud.marca(),
                solicitud.modelo(),
                solicitud.color());
        return VehiculoResponse.desde(vehiculo);
    }

    @GetMapping("/me")
    public List<VehiculoResponse> listar(Authentication autenticacion) {
        Integer idUsuario = usuarioService.buscarPorCorreoAutenticado(autenticacion.getName()).getId();
        return servicio.listarPorUsuario(idUsuario).stream().map(VehiculoResponse::desde).toList();
    }
}
