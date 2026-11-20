package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.SaldoHorasGratuitasResponse;
import co.edu.poli.parqueadero.servicios.SaldoHorasGratuitasService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios/{idUsuario}/saldo-horas-gratuitas")
public class SaldoHorasGratuitasController {

    private final SaldoHorasGratuitasService servicio;

    public SaldoHorasGratuitasController(SaldoHorasGratuitasService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public SaldoHorasGratuitasResponse consultar(@PathVariable Integer idUsuario) {
        return servicio.consultar(idUsuario);
    }
}
