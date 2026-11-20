package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Evidencia inmutable de un pago exitoso de parqueadero. */
public record ComprobantePago(
        Integer idSesion,
        String placa,
        LocalDateTime fechaHoraPago,
        long minutosEstacionamiento,
        String tiempoEstacionamiento,
        BigDecimal horasGratuitas,
        BigDecimal valorPagado) {
}
