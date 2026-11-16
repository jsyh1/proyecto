package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.ResultadoValidacionQr;
import co.edu.poli.parqueadero.servicios.ValidacionQrResponse;
import co.edu.poli.parqueadero.servicios.ValidacionQrService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ValidacionQrController {

    private final ValidacionQrService servicio;

    public ValidacionQrController(ValidacionQrService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/sesiones/{idSesion}/beneficios/validar")
    public ResultadoValidacionQr validar(
            Authentication autenticacion,
            @PathVariable Integer idSesion,
            @Valid @RequestBody SolicitudValidacion solicitud) {
        return servicio.validar(autenticacion.getName(), idSesion, solicitud.codigo());
    }

    @GetMapping("/validaciones")
    public List<ValidacionQrResponse> historial(Authentication autenticacion) {
        return servicio.historial(autenticacion.getName());
    }

    public record SolicitudValidacion(@NotBlank String codigo) {
    }
}
