package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;

public record ResultadoPago(
        boolean aprobado,
        String mensaje,
        Integer idSesion,
        BigDecimal costoInicial,
        BigDecimal descuentoBeneficios,
        BigDecimal valorFinal) {
}
