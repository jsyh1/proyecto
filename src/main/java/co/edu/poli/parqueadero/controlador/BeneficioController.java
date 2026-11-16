package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.BeneficioResponse;
import co.edu.poli.parqueadero.servicios.BeneficioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BeneficioController {

    private final BeneficioService servicio;

    public BeneficioController(BeneficioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/beneficios")
    public List<BeneficioResponse> listar(@RequestParam Integer idCentro) {
        return servicio.listar(idCentro);
    }

    @PostMapping("/admin/beneficios")
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficioResponse crear(@Valid @RequestBody SolicitudBeneficio solicitud) {
        return servicio.crear(solicitud.aServicio());
    }

    @PutMapping("/admin/beneficios/{id}")
    public BeneficioResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody SolicitudBeneficio solicitud) {
        return servicio.actualizar(id, solicitud.aServicio());
    }

    @PostMapping("/admin/beneficios/{id}/codigos")
    @ResponseStatus(HttpStatus.CREATED)
    public List<String> emitirCodigos(
            @PathVariable Integer id,
            @Valid @RequestBody SolicitudCodigos solicitud) {
        return servicio.emitirCodigos(id, solicitud.cantidad(), solicitud.vencimiento());
    }

    public record SolicitudBeneficio(
            @NotNull Integer idCentro,
            @NotBlank String nombre,
            @NotBlank String tipo,
            @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
            @NotNull LocalDate fechaInicio,
            LocalDate fechaFin,
            boolean activo) {

        private BeneficioService.SolicitudBeneficio aServicio() {
            return new BeneficioService.SolicitudBeneficio(
                    idCentro, nombre, tipo, valor, fechaInicio, fechaFin, activo);
        }
    }

    public record SolicitudCodigos(
            @Positive int cantidad,
            @NotNull LocalDateTime vencimiento) {
    }
}
