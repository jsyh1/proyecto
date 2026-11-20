package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.SaldoHorasGratuitasResponse;
import co.edu.poli.parqueadero.servicios.SaldoHorasGratuitasService;
import co.edu.poli.parqueadero.seguridad.AutorizacionUsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios/{idUsuario}/saldo-horas-gratuitas")
public class SaldoHorasGratuitasController {

    private final SaldoHorasGratuitasService servicio;
    private final AutorizacionUsuarioService autorizacion;

    public SaldoHorasGratuitasController(
            SaldoHorasGratuitasService servicio, AutorizacionUsuarioService autorizacion) {
        this.servicio = servicio;
        this.autorizacion = autorizacion;
    }

    @GetMapping
    public SaldoHorasGratuitasResponse consultar(
            @PathVariable Integer idUsuario, Authentication autenticacion) {
        autorizacion.validarAcceso(idUsuario, autenticacion);
        return servicio.consultar(idUsuario);
    }
}
