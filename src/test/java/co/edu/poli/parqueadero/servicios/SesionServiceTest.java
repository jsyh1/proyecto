package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SesionServiceTest {

    @Test
    void normalizaLaPlaca() {
        assertEquals("ABC123", SesionService.normalizarPlaca(" abc-123 "));
        assertEquals("", SesionService.normalizarPlaca(null));
    }

    @Test
    void cobraProporcionalAlMinuto() {
        // 2 h 15 min a $5.000 la hora = $11.250
        BigDecimal costo = SesionService.calcularCosto(135, BigDecimal.ZERO, new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("11250.00"), costo);
    }

    @Test
    void descuentaLasHorasGratuitas() {
        // 2 h 15 min con 1 h gratis = 75 min facturables = $6.250
        BigDecimal costo = SesionService.calcularCosto(135, BigDecimal.ONE, new BigDecimal("5000.00"));
        assertEquals(new BigDecimal("6250.00"), costo);
    }

    @Test
    void elCostoNuncaEsNegativo() {
        BigDecimal costo = SesionService.calcularCosto(30, new BigDecimal("2"), new BigDecimal("5000.00"));
        assertEquals(0, costo.compareTo(BigDecimal.ZERO));
    }

    @Test
    void formateaLaDuracion() {
        assertEquals("45 min", SesionService.formatearDuracion(45));
        assertEquals("2 h 15 min", SesionService.formatearDuracion(135));
    }
}
