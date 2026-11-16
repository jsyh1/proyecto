package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.modelo.Beneficio;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BeneficioResponse(
        Integer id,
        Integer idCentro,
        String centro,
        String nombre,
        String tipo,
        BigDecimal valor,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean activo) {

    public static BeneficioResponse desde(Beneficio beneficio) {
        return new BeneficioResponse(beneficio.getId(), beneficio.getCentro().getId(),
                beneficio.getCentro().getNombre(), beneficio.getNombre(), beneficio.getTipo(),
                beneficio.getValor(), beneficio.getFechaInicio(), beneficio.getFechaFin(),
                beneficio.isActivo());
    }
}
