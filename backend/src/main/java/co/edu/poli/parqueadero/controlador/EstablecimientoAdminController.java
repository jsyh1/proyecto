package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.ComercioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/establecimientos")
public class EstablecimientoAdminController {

    private final ComercioService servicio;

    public EstablecimientoAdminController(ComercioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<EstablecimientoResponse> listar() {
        return servicio.consultarEstablecimientos().stream()
                .map(EstablecimientoResponse::desde).toList();
    }

    @GetMapping("/{id}")
    public EstablecimientoResponse consultar(@PathVariable Integer id) {
        return EstablecimientoResponse.desde(servicio.consultarEstablecimiento(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstablecimientoResponse registrar(@Valid @RequestBody EstablecimientoRequest solicitud) {
        return EstablecimientoResponse.desde(
                servicio.registrarEstablecimiento(solicitud.nombre(), solicitud.direccion()));
    }

    @PutMapping("/{id}")
    public EstablecimientoResponse modificar(
            @PathVariable Integer id,
            @Valid @RequestBody EstablecimientoRequest solicitud) {
        return EstablecimientoResponse.desde(
                servicio.modificarEstablecimiento(id, solicitud.nombre(), solicitud.direccion()));
    }

    @PostMapping("/{id}/activar")
    public EstablecimientoResponse activar(@PathVariable Integer id) {
        return EstablecimientoResponse.desde(servicio.activarEstablecimiento(id));
    }

    @PostMapping("/{id}/desactivar")
    public EstablecimientoResponse desactivar(@PathVariable Integer id) {
        return EstablecimientoResponse.desde(servicio.desactivarEstablecimiento(id));
    }
}
