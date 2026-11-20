package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.modelo.Vehiculo;
import co.edu.poli.parqueadero.servicios.VehiculoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vehiculos")
public class VehiculoController {

    private final VehiculoService servicio;

    public VehiculoController(VehiculoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponse registrar(@Valid @RequestBody RegistrarVehiculoRequest solicitud) {
        Vehiculo vehiculo = servicio.registrar(
                solicitud.idUsuario(),
                solicitud.placa(),
                solicitud.marca(),
                solicitud.modelo(),
                solicitud.color());
        return VehiculoResponse.desde(vehiculo);
    }

    @GetMapping
    public List<VehiculoResponse> listarPorUsuario(@RequestParam Integer idUsuario) {
        return servicio.listarPorUsuario(idUsuario).stream()
                .map(VehiculoResponse::desde)
                .toList();
    }
}
