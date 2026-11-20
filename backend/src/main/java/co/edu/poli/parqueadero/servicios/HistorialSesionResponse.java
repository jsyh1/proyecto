package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;

public record HistorialSesionResponse(
        Integer id,
        String placa,
        String centro,
        LocalDateTime fechaEntrada,
        LocalDateTime fechaSalida,
        String estado,
        BigDecimal montoPagado) {

    static HistorialSesionResponse desde(SesionParqueadero sesion) {
        return new HistorialSesionResponse(
                sesion.getId(),
                sesion.getVehiculo().getPlaca(),
                sesion.getCentro().getNombre(),
                sesion.getFechaEntrada(),
                sesion.getFechaSalida(),
                sesion.getEstado(),
                sesion.getMontoTotal());
    }
}
