package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;

public record ResultadoValidacionQr(
        boolean aprobada,
        String mensaje,
        String beneficio,
        String tipo,
        BigDecimal valor) {
}
