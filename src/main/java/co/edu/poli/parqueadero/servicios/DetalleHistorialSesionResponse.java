package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;

public record DetalleHistorialSesionResponse(
        Integer id,
        String placa,
        String centro,
        LocalDateTime fechaEntrada,
        LocalDateTime fechaSalida,
        String estado,
        BigDecimal valorHora,
        BigDecimal horasFacturables,
        BigDecimal horasGratuitas,
        BigDecimal montoPagado) {

    static DetalleHistorialSesionResponse desde(SesionParqueadero sesion) {
        return new DetalleHistorialSesionResponse(
                sesion.getId(),
                sesion.getVehiculo().getPlaca(),
                sesion.getCentro().getNombre(),
                sesion.getFechaEntrada(),
                sesion.getFechaSalida(),
                sesion.getEstado(),
                sesion.getTarifa().getValorHora(),
                sesion.getHorasFacturables(),
                sesion.getHorasGratuitas(),
                sesion.getMontoTotal());
    }
}
