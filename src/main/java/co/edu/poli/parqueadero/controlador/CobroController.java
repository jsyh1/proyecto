package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.CobroService;
import co.edu.poli.parqueadero.servicios.PagoService;
import co.edu.poli.parqueadero.servicios.ResumenCobro;
import co.edu.poli.parqueadero.servicios.ResultadoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sesiones/{idSesion}")
public class CobroController {

    private final CobroService cobroService;
    private final PagoService pagoService;

    public CobroController(CobroService cobroService, PagoService pagoService) {
        this.cobroService = cobroService;
        this.pagoService = pagoService;
    }

    @GetMapping("/pago")
    public ResumenCobro consultar(Authentication autenticacion, @PathVariable Integer idSesion) {
        return cobroService.consultar(autenticacion.getName(), idSesion);
    }

    @PostMapping("/pagos")
    public ResultadoPago pagar(
            Authentication autenticacion,
            @PathVariable Integer idSesion,
            @Valid @RequestBody SolicitudPago solicitud) {
        return pagoService.pagar(autenticacion.getName(), idSesion, solicitud.metodo());
    }

    public record SolicitudPago(@NotBlank String metodo) {
    }
}
