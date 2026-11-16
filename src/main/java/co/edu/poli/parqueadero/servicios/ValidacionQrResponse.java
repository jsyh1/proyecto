package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.modelo.ValidacionQr;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ValidacionQrResponse(
        Integer id,
        Integer idSesion,
        String centro,
        String beneficio,
        String resultado,
        String motivo,
        BigDecimal descuentoAplicado,
        LocalDateTime fecha) {

    public static ValidacionQrResponse desde(ValidacionQr validacion) {
        return new ValidacionQrResponse(
                validacion.getId(),
                validacion.getSesion().getId(),
                validacion.getSesion().getCentro().getNombre(),
                validacion.getQr() == null ? null : validacion.getQr().getBeneficio().getNombre(),
                validacion.getResultado(),
                validacion.getMotivo(),
                validacion.getDescuentoAplicado(),
                validacion.getFechaValidacion());
    }
}
