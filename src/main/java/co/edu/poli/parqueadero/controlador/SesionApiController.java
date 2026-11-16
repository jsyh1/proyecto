package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.servicios.ResumenSesion;
import co.edu.poli.parqueadero.servicios.SesionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/sesiones")
public class SesionApiController {

    private final SesionService servicio;

    public SesionApiController(SesionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumenSesion registrarEntrada(
            Authentication autenticacion,
            @Valid @RequestBody SolicitudEntrada solicitud) {
        SesionParqueadero sesion = servicio.registrarEntrada(
                solicitud.placa(), solicitud.idCentro(), autenticacion.getName());
        return servicio.consultarSesion(sesion.getId());
    }

    @GetMapping("/consulta")
    public ResumenSesion consultar(Authentication autenticacion, @RequestParam @NotBlank String placa) {
        return servicio.consultarSesionAbiertaPorPlaca(placa, autenticacion.getName());
    }

    public record SolicitudEntrada(@NotBlank String placa, @NotNull Integer idCentro) {
    }
}
