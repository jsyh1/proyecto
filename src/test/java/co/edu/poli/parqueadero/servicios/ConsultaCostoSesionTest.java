package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.RegistroAccesoDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.dao.VehiculoDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Tarifa;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import org.junit.jupiter.api.Test;

class ConsultaCostoSesionTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Test
    void actualizaElCostoAcumuladoCadaVezQueConsultaUnaSesionAbierta() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        SesionParqueadero sesion = crearSesion(SesionParqueadero.ESTADO_ABIERTA);
        sesion.setMontoTotal(new BigDecimal("9000.00"));
        when(sesionDAO.buscarAbiertaPorPlaca("ABC123")).thenReturn(sesion);

        ResumenSesion primerResumen = crearServicio(sesionDAO, "2026-11-14T17:15:00Z")
                .consultarSesionAbiertaPorPlaca("ABC123");

        assertEquals(135, primerResumen.minutosTranscurridos());
        assertEquals(new BigDecimal("11250.00"), primerResumen.costoActual());
        assertEquals(new BigDecimal("11250.00"), sesion.getMontoTotal());

        ResumenSesion segundoResumen = crearServicio(sesionDAO, "2026-11-14T18:15:00Z")
                .consultarSesionAbiertaPorPlaca("ABC123");

        assertEquals(195, segundoResumen.minutosTranscurridos());
        assertEquals(new BigDecimal("16250.00"), segundoResumen.costoActual());
        assertEquals(new BigDecimal("16250.00"), sesion.getMontoTotal());
    }

    @Test
    void conservaElMontoFinalDeUnaSesionCerrada() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        SesionParqueadero sesion = crearSesion("CERRADA");
        sesion.setFechaSalida(LocalDateTime.of(2026, 11, 14, 12, 0));
        sesion.setMontoTotal(new BigDecimal("10000.00"));
        when(sesionDAO.buscarPorId(1)).thenReturn(sesion);

        ResumenSesion resumen = crearServicio(sesionDAO, "2026-11-14T18:15:00Z")
                .consultarSesion(1);

        assertEquals(new BigDecimal("10000.00"), resumen.costoActual());
        assertEquals(new BigDecimal("10000.00"), sesion.getMontoTotal());
    }

    private SesionService crearServicio(SesionDAO sesionDAO, String instante) {
        return new SesionService(
                mock(VehiculoDAO.class),
                sesionDAO,
                mock(TarifaDAO.class),
                mock(CentroComercialDAO.class),
                mock(RegistroAccesoDAO.class),
                mock(NotificacionService.class),
                Clock.fixed(Instant.parse(instante), ZONA));
    }

    private SesionParqueadero crearSesion(String estado) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("ABC123");
        CentroComercial centro = new CentroComercial();
        centro.setNombre("Centro Demo");
        Tarifa tarifa = new Tarifa();
        tarifa.setValorHora(new BigDecimal("5000.00"));

        SesionParqueadero sesion = new SesionParqueadero();
        sesion.setId(1);
        sesion.setVehiculo(vehiculo);
        sesion.setCentro(centro);
        sesion.setTarifa(tarifa);
        sesion.setFechaEntrada(LocalDateTime.of(2026, 11, 14, 10, 0));
        sesion.setHorasGratuitas(BigDecimal.ZERO);
        sesion.setEstado(estado);
        return sesion;
    }
}
