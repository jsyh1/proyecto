package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Vehiculo;

class ComprobantePagoServiceTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Test
    void generaComprobanteConDatosDeSesionPagoYBeneficios() {
        SesionParqueadero sesion = crearSesionCerrada();
        sesion.setHorasGratuitas(BigDecimal.ONE);

        ComprobantePago comprobante = crearServicio().generarComprobantePagoExitoso(
                sesion, new BigDecimal("6250.00"));

        assertEquals(1, comprobante.idSesion());
        assertEquals("ABC123", comprobante.placa());
        assertEquals(LocalDateTime.of(2026, 11, 14, 13, 15), comprobante.fechaHoraPago());
        assertEquals(135, comprobante.minutosEstacionamiento());
        assertEquals("2 h 15 min", comprobante.tiempoEstacionamiento());
        assertEquals(BigDecimal.ONE, comprobante.horasGratuitas());
        assertEquals(new BigDecimal("6250.00"), comprobante.valorPagado());
    }

    @Test
    void noGeneraComprobanteParaUnaSesionAbierta() {
        SesionParqueadero sesion = crearSesionCerrada();
        sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);

        assertThrows(ReglaNegocioException.class,
                () -> crearServicio().generarComprobantePagoExitoso(sesion, new BigDecimal("6250.00")));
    }

    @Test
    void rechazaUnValorPagadoNegativo() {
        assertThrows(ReglaNegocioException.class,
                () -> crearServicio().generarComprobantePagoExitoso(
                        crearSesionCerrada(), new BigDecimal("-1.00")));
    }

    private ComprobantePagoService crearServicio() {
        return new ComprobantePagoService(Clock.fixed(Instant.parse("2026-11-14T18:15:00Z"), ZONA));
    }

    private SesionParqueadero crearSesionCerrada() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("ABC123");

        SesionParqueadero sesion = new SesionParqueadero();
        sesion.setId(1);
        sesion.setVehiculo(vehiculo);
        sesion.setFechaEntrada(LocalDateTime.of(2026, 11, 14, 10, 0));
        sesion.setFechaSalida(LocalDateTime.of(2026, 11, 14, 12, 15));
        sesion.setEstado(SesionParqueadero.ESTADO_CERRADA);
        return sesion;
    }
}
