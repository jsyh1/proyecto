package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.BeneficioQrDAO;
import co.edu.poli.parqueadero.dao.RegistroAccesoDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.dao.VehiculoDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.RegistroAcceso;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Tarifa;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;

class RegistrarSalidaSesionTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Test
    void cierraLaSesionCalculaElValorFinalYRegistraElEventoDeSalida() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        RegistroAccesoDAO accesoDAO = mock(RegistroAccesoDAO.class);
        SesionParqueadero sesion = crearSesionAbierta();
        sesion.setHorasGratuitas(BigDecimal.ONE);
        when(sesionDAO.buscarAbiertaPorPlacaBloqueada("ABC123")).thenReturn(sesion);

        ResumenSesion resumen = crearServicio(sesionDAO, accesoDAO, "2026-11-14T17:15:00Z")
                .registrarSalidaPorPlaca(" abc-123 ");

        assertEquals(SesionParqueadero.ESTADO_CERRADA, resumen.estado());
        assertEquals("14/11/2026 12:15", resumen.fechaSalida());
        assertEquals(135, resumen.minutosTranscurridos());
        assertEquals(new BigDecimal("1.25"), resumen.horasFacturables());
        assertEquals(BigDecimal.ONE, resumen.horasGratuitas());
        assertEquals(0, resumen.beneficiosQrAplicados());
        assertEquals(new BigDecimal("1.25"), sesion.getHorasFacturables());
        assertEquals(new BigDecimal("6250.00"), resumen.costoActual());
        assertEquals(LocalDateTime.of(2026, 11, 14, 12, 15), sesion.getFechaSalida());

        ArgumentCaptor<RegistroAcceso> accesoCaptor = ArgumentCaptor.forClass(RegistroAcceso.class);
        verify(accesoDAO).crear(accesoCaptor.capture());
        RegistroAcceso acceso = accesoCaptor.getValue();
        assertEquals(RegistroAcceso.EVENTO_SALIDA, acceso.getTipoEvento());
        assertEquals(RegistroAcceso.RESULTADO_RECONOCIDO, acceso.getResultadoDeteccion());
        assertEquals(sesion, acceso.getSesion());
        assertEquals("ABC123", acceso.getPlacaDetectada());
    }

    @Test
    void rechazaPlacaInvalidaOSinSesionAbierta() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        RegistroAccesoDAO accesoDAO = mock(RegistroAccesoDAO.class);
        SesionService servicio = crearServicio(sesionDAO, accesoDAO, "2026-11-14T17:15:00Z");

        assertThrows(ReglaNegocioException.class, () -> servicio.registrarSalidaPorPlaca("??"));
        when(sesionDAO.buscarAbiertaPorPlacaBloqueada("ABC123")).thenReturn(null);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrarSalidaPorPlaca("ABC123"));

        verify(accesoDAO, never()).crear(any(RegistroAcceso.class));
    }

    @Test
    void rechazaUnaHoraDeSalidaAnteriorALaEntrada() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        SesionParqueadero sesion = crearSesionAbierta();
        when(sesionDAO.buscarAbiertaPorPlacaBloqueada("ABC123")).thenReturn(sesion);

        assertThrows(ReglaNegocioException.class,
                () -> crearServicio(sesionDAO, mock(RegistroAccesoDAO.class), "2026-11-14T14:59:00Z")
                        .registrarSalidaPorPlaca("ABC123"));

        assertEquals(SesionParqueadero.ESTADO_ABIERTA, sesion.getEstado());
        assertNull(sesion.getFechaSalida());
    }

    @Test
    void devuelveAlSaldoLaParteDeLaBonificacionQueNoSeUtilizo() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        SesionParqueadero sesion = crearSesionAbierta();
        sesion.setHorasGratuitas(new BigDecimal("2.000000"));
        when(sesionDAO.buscarAbiertaPorPlacaBloqueada("ABC123")).thenReturn(sesion);
        SaldoHorasGratuitasService saldoHorasService = mock(SaldoHorasGratuitasService.class);

        crearServicio(sesionDAO, mock(RegistroAccesoDAO.class), saldoHorasService, "2026-11-14T15:15:00Z")
                .registrarSalidaPorPlaca("ABC123");

        assertEquals(new BigDecimal("0.250000"), sesion.getHorasGratuitas());
        verify(saldoHorasService).finalizarUsoEnSesion(
                8, 1, new BigDecimal("2.000000"), new BigDecimal("0.250000"));
    }

    @Test
    void reservaElSaldoDisponibleAlIniciarUnaSesion() {
        VehiculoDAO vehiculoDAO = mock(VehiculoDAO.class);
        SesionDAO sesionDAO = mock(SesionDAO.class);
        CentroComercialDAO centroDAO = mock(CentroComercialDAO.class);
        TarifaDAO tarifaDAO = mock(TarifaDAO.class);
        SaldoHorasGratuitasService saldoHorasService = mock(SaldoHorasGratuitasService.class);
        Vehiculo vehiculo = crearSesionAbierta().getVehiculo();
        CentroComercial centro = new CentroComercial();
        centro.setId(2);
        Tarifa tarifa = new Tarifa();
        tarifa.setValorHora(new BigDecimal("5000.00"));
        when(vehiculoDAO.buscarPorPlacaBloqueado("ABC123")).thenReturn(vehiculo);
        when(centroDAO.buscarPorId(2)).thenReturn(centro);
        when(tarifaDAO.buscarVigente(org.mockito.ArgumentMatchers.eq(2), any()))
                .thenReturn(tarifa);
        when(sesionDAO.crear(any(SesionParqueadero.class))).thenAnswer((InvocationOnMock llamada) -> {
            SesionParqueadero nuevaSesion = llamada.getArgument(0);
            nuevaSesion.setId(41);
            return true;
        });
        when(saldoHorasService.reservarSaldoDisponible(8, 41)).thenReturn(new BigDecimal("1.250000"));
        SesionService servicio = new SesionService(
                vehiculoDAO,
                sesionDAO,
                mock(BeneficioQrDAO.class),
                tarifaDAO,
                centroDAO,
                mock(RegistroAccesoDAO.class),
                saldoHorasService,
                Clock.fixed(Instant.parse("2026-11-14T15:00:00Z"), ZONA));

        SesionParqueadero sesion = servicio.registrarEntrada("ABC123", 2);

        assertEquals(new BigDecimal("1.250000"), sesion.getHorasGratuitas());
        verify(saldoHorasService).reservarSaldoDisponible(8, 41);
    }

    private SesionService crearServicio(SesionDAO sesionDAO, RegistroAccesoDAO accesoDAO, String instante) {
        return crearServicio(sesionDAO, accesoDAO, mock(SaldoHorasGratuitasService.class), instante);
    }

    private SesionService crearServicio(
            SesionDAO sesionDAO,
            RegistroAccesoDAO accesoDAO,
            SaldoHorasGratuitasService saldoHorasService,
            String instante) {
        return new SesionService(
                mock(VehiculoDAO.class),
                sesionDAO,
                mock(BeneficioQrDAO.class),
                mock(TarifaDAO.class),
                mock(CentroComercialDAO.class),
                accesoDAO,
                saldoHorasService,
                Clock.fixed(Instant.parse(instante), ZONA));
    }

    private SesionParqueadero crearSesionAbierta() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("ABC123");
        Usuario usuario = new Usuario();
        usuario.setId(8);
        vehiculo.setUsuario(usuario);
        CentroComercial centro = new CentroComercial();
        centro.setId(1);
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
        sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);
        return sesion;
    }
}
