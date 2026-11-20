package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaldoHorasGratuitasResponse(
        Integer idUsuario,
        BigDecimal saldoHoras,
        List<Movimiento> movimientos) {

    public record Movimiento(LocalDateTime fechaHora, BigDecimal cantidadHoras, String motivo) {
    }
}
