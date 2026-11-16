package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.servicios.TarifaResponse;
import co.edu.poli.parqueadero.servicios.TarifaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
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
public class TarifaController {

    private final TarifaService tarifaService;
    private final CentroComercialDAO centroDAO;

    public TarifaController(TarifaService tarifaService, CentroComercialDAO centroDAO) {
        this.tarifaService = tarifaService;
        this.centroDAO = centroDAO;
    }

    @GetMapping("/centros")
    public List<CentroResponse> centros() {
        return centroDAO.listarActivos().stream()
                .map(centro -> new CentroResponse(centro.getId(), centro.getNombre(), centro.getDireccion()))
                .toList();
    }

    @GetMapping("/tarifas/vigentes")
    public TarifaResponse vigente(@RequestParam Integer idCentro) {
        return tarifaService.vigente(idCentro);
    }

    @GetMapping("/admin/tarifas")
    public List<TarifaResponse> listar(@RequestParam Integer idCentro) {
        return tarifaService.listar(idCentro);
    }

    @PostMapping("/admin/tarifas")
    @ResponseStatus(HttpStatus.CREATED)
    public TarifaResponse crear(@Valid @RequestBody SolicitudTarifa solicitud) {
        return tarifaService.crear(solicitud.aServicio());
    }

    @PutMapping("/admin/tarifas/{id}")
    public TarifaResponse actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody SolicitudTarifa solicitud) {
        return tarifaService.actualizar(id, solicitud.aServicio());
    }

    public record CentroResponse(Integer id, String nombre, String direccion) {
    }

    public record SolicitudTarifa(
            @NotNull Integer idCentro,
            @NotNull @DecimalMin(value = "0.01") BigDecimal valorHora,
            @NotNull LocalDate fechaInicio,
            LocalDate fechaFin,
            @NotNull String estado) {

        private TarifaService.SolicitudTarifa aServicio() {
            return new TarifaService.SolicitudTarifa(idCentro, valorHora, fechaInicio, fechaFin, estado);
        }
    }
}
