package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;

/** Datos de una sesión listos para mostrar: tiempo transcurrido y costo acumulado. */
public record ResumenSesion(
        Integer id,
        String placa,
        String centro,
        String fechaEntrada,
        String estado,
        long minutosTranscurridos,
        String tiempoTranscurrido,
        BigDecimal valorHora,
        BigDecimal horasGratuitas,
        BigDecimal costoActual) {
}
