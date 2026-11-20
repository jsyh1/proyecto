package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;

@Service
public class ComprobantePagoService {

    private final Clock reloj;

    public ComprobantePagoService(Clock reloj) {
        this.reloj = reloj;
    }

    /**
     * Genera el comprobante desde el flujo que ya confirmó el pago exitoso.
     */
    public ComprobantePago generarComprobantePagoExitoso(SesionParqueadero sesion, BigDecimal valorPagado) {
        if (sesion == null || sesion.getId() == null || sesion.getVehiculo() == null
                || sesion.getVehiculo().getPlaca() == null || sesion.getVehiculo().getPlaca().isBlank()) {
            throw new ReglaNegocioException("No hay información suficiente de la sesión para generar el comprobante.");
        }
        if (!SesionParqueadero.ESTADO_CERRADA.equals(sesion.getEstado())
                || sesion.getFechaEntrada() == null || sesion.getFechaSalida() == null) {
            throw new ReglaNegocioException("El comprobante solo se puede generar para una sesión cerrada.");
        }
        if (sesion.getFechaSalida().isBefore(sesion.getFechaEntrada())) {
            throw new ReglaNegocioException("La fecha de salida no puede ser anterior a la fecha de entrada.");
        }
        if (valorPagado == null || valorPagado.signum() < 0) {
            throw new ReglaNegocioException("El valor pagado no es válido.");
        }

        long minutosEstacionamiento = Duration.between(sesion.getFechaEntrada(), sesion.getFechaSalida()).toMinutes();
        BigDecimal horasGratuitas = sesion.getHorasGratuitas() != null
                ? sesion.getHorasGratuitas()
                : BigDecimal.ZERO;
        return new ComprobantePago(
                sesion.getId(),
                sesion.getVehiculo().getPlaca(),
                LocalDateTime.now(reloj),
                minutosEstacionamiento,
                SesionService.formatearDuracion(minutosEstacionamiento),
                horasGratuitas,
                valorPagado);
    }
}
