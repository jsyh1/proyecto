package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.modelo.Tarifa;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TarifaResponse(
        Integer id,
        Integer idCentro,
        String centro,
        BigDecimal valorHora,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        String estado) {

    public static TarifaResponse desde(Tarifa tarifa) {
        return new TarifaResponse(tarifa.getId(), tarifa.getCentro().getId(),
                tarifa.getCentro().getNombre(), tarifa.getValorHora(),
                tarifa.getFechaInicio(), tarifa.getFechaFin(), tarifa.getEstado());
    }
}
