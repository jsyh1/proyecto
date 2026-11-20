package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import co.edu.poli.parqueadero.modelo.BeneficioQrAplicado;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Tarifa;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import org.junit.jupiter.api.Test;

class ConsultaCostoSesionTest {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    @Test
    void actualizaElCostoAcumuladoCadaVezQueConsultaUnaSesionAbierta() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        SesionParqueadero sesion = crearSesion(SesionParqueadero.ESTADO_ABIERTA);
        sesion.setMontoTotal(new BigDecimal("9000.00"));
        when(sesionDAO.buscarAbiertaPorPlacaBloqueada("ABC123")).thenReturn(sesion);

        ResumenSesion primerResumen = crearServicio(sesionDAO, "2026-11-14T17:15:00Z")
                .consultarSesionAbiertaPorPlaca("ABC123");

        assertEquals(135, primerResumen.minutosTranscurridos());
        assertEquals(new BigDecimal("2.25"), primerResumen.horasFacturables());
        assertEquals(new BigDecimal("11250.00"), primerResumen.costoActual());
        assertEquals(new BigDecimal("11250.00"), sesion.getMontoTotal());

        ResumenSesion segundoResumen = crearServicio(sesionDAO, "2026-11-14T18:15:00Z")
                .consultarSesionAbiertaPorPlaca("ABC123");

        assertEquals(195, segundoResumen.minutosTranscurridos());
        assertEquals(new BigDecimal("3.25"), segundoResumen.horasFacturables());
        assertEquals(new BigDecimal("16250.00"), segundoResumen.costoActual());
        assertEquals(new BigDecimal("16250.00"), sesion.getMontoTotal());
    }

    @Test
    void conservaElMontoFinalDeUnaSesionCerrada() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        SesionParqueadero sesion = crearSesion("CERRADA");
        sesion.setFechaSalida(LocalDateTime.of(2026, 11, 14, 12, 0));
        sesion.setMontoTotal(new BigDecimal("10000.00"));
        when(sesionDAO.buscarPorIdBloqueado(1)).thenReturn(sesion);
        ResumenSesion resumen = crearServicio(sesionDAO, "2026-11-14T18:15:00Z")
                .consultarSesion(1);

        assertEquals(new BigDecimal("10000.00"), resumen.costoActual());
        assertEquals(new BigDecimal("10000.00"), sesion.getMontoTotal());
    }

    @Test
    void aplicaUnaHoraGratisPorCadaCompraValidaYActualizaElCosto() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        SesionParqueadero sesion = crearSesion(SesionParqueadero.ESTADO_ABIERTA);
        BeneficioQrDAO beneficioQrDAO = mock(BeneficioQrDAO.class);
        when(sesionDAO.buscarPorIdBloqueado(1)).thenReturn(sesion);
        when(beneficioQrDAO.buscarPorClaveCompraHash(org.mockito.ArgumentMatchers.anyString())).thenReturn(null);
        SesionService servicio = crearServicio(sesionDAO, beneficioQrDAO,
                mock(SaldoHorasGratuitasService.class), "2026-11-14T17:15:00Z");

        ResumenSesion primerResumen = servicio.aplicarBeneficioCompraValida(1, "compra-qr-1");

        assertEquals(BigDecimal.ONE, primerResumen.horasGratuitas());
        assertEquals(1, primerResumen.beneficiosQrAplicados());
        assertEquals(new BigDecimal("1.25"), primerResumen.horasFacturables());
        assertEquals(new BigDecimal("6250.00"), primerResumen.costoActual());
        assertEquals(new BigDecimal("6250.00"), sesion.getMontoTotal());

        ResumenSesion segundoResumen = servicio.aplicarBeneficioCompraValida(1, "compra-qr-2");

        assertEquals(new BigDecimal("2"), segundoResumen.horasGratuitas());
        assertEquals(2, segundoResumen.beneficiosQrAplicados());
        assertEquals(new BigDecimal("0.25"), segundoResumen.horasFacturables());
        assertEquals(new BigDecimal("1250.00"), segundoResumen.costoActual());
        assertEquals(new BigDecimal("1250.00"), sesion.getMontoTotal());
    }

    @Test
    void noAplicaDosVecesLaMismaCompraQr() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        BeneficioQrDAO beneficioQrDAO = mock(BeneficioQrDAO.class);
        SaldoHorasGratuitasService saldoHorasService = mock(SaldoHorasGratuitasService.class);
        SesionParqueadero sesion = crearSesion(SesionParqueadero.ESTADO_ABIERTA);
        BeneficioQrAplicado beneficio = new BeneficioQrAplicado();
        beneficio.setSesion(sesion);
        when(sesionDAO.buscarPorIdBloqueado(1)).thenReturn(sesion);
        when(beneficioQrDAO.buscarPorClaveCompraHash(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(null, beneficio);
        when(saldoHorasService.acreditarCompraYReservar(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any())).thenReturn(BigDecimal.ONE);
        SesionService servicio = crearServicio(
                sesionDAO, beneficioQrDAO, saldoHorasService, "2026-11-14T17:15:00Z");

        servicio.aplicarBeneficioCompraValida(1, "compra-qr-unica");
        ResumenSesion reintento = servicio.aplicarBeneficioCompraValida(1, "compra-qr-unica");

        assertEquals(1, reintento.beneficiosQrAplicados());
        assertEquals(BigDecimal.ONE, reintento.horasGratuitas());
        org.mockito.Mockito.verify(saldoHorasService, org.mockito.Mockito.times(1))
                .acreditarCompraYReservar(org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaReutilizarLaClaveDeCompraEnOtraSesion() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        BeneficioQrDAO beneficioQrDAO = mock(BeneficioQrDAO.class);
        SaldoHorasGratuitasService saldoHorasService = mock(SaldoHorasGratuitasService.class);
        SesionParqueadero sesion = crearSesion(SesionParqueadero.ESTADO_ABIERTA);
        SesionParqueadero otraSesion = crearSesion(SesionParqueadero.ESTADO_ABIERTA);
        otraSesion.setId(2);
        BeneficioQrAplicado beneficio = new BeneficioQrAplicado();
        beneficio.setSesion(otraSesion);
        when(sesionDAO.buscarPorIdBloqueado(1)).thenReturn(sesion);
        when(beneficioQrDAO.buscarPorClaveCompraHash(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(beneficio);
        SesionService servicio = crearServicio(
                sesionDAO, beneficioQrDAO, saldoHorasService, "2026-11-14T17:15:00Z");

        assertThrows(ReglaNegocioException.class,
                () -> servicio.aplicarBeneficioCompraValida(1, "compra-qr-reutilizada"));

        assertEquals(BigDecimal.ZERO, sesion.getHorasGratuitas());
        verify(saldoHorasService, never()).acreditarCompraYReservar(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaAplicarBeneficioASesionCerrada() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        SesionParqueadero sesion = crearSesion("CERRADA");
        when(sesionDAO.buscarPorIdBloqueado(1)).thenReturn(sesion);

        assertThrows(ReglaNegocioException.class,
                () -> crearServicio(sesionDAO, "2026-11-14T17:15:00Z")
                        .aplicarBeneficioCompraValida(1, "compra-qr"));
        assertEquals(BigDecimal.ZERO, sesion.getHorasGratuitas());
    }

    @Test
    void informaCuandoLaSesionDelBeneficioNoExiste() {
        SesionDAO sesionDAO = mock(SesionDAO.class);

        assertThrows(ReglaNegocioException.class,
                () -> crearServicio(sesionDAO, "2026-11-14T17:15:00Z")
                        .aplicarBeneficioCompraValida(99, "compra-qr"));
    }

    private SesionService crearServicio(SesionDAO sesionDAO, String instante) {
        return crearServicio(sesionDAO, mock(BeneficioQrDAO.class), mock(SaldoHorasGratuitasService.class), instante);
    }

    private SesionService crearServicio(
            SesionDAO sesionDAO,
            BeneficioQrDAO beneficioQrDAO,
            SaldoHorasGratuitasService saldoHorasService,
            String instante) {
        when(saldoHorasService.reservarSaldoDisponible(org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt())).thenReturn(BigDecimal.ZERO);
        when(saldoHorasService.acreditarCompraYReservar(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any())).thenReturn(BigDecimal.ONE);
        return new SesionService(
                mock(VehiculoDAO.class),
                sesionDAO,
                beneficioQrDAO,
                mock(TarifaDAO.class),
                mock(CentroComercialDAO.class),
                mock(RegistroAccesoDAO.class),
                saldoHorasService,
                Clock.fixed(Instant.parse(instante), ZONA));
    }

    private SesionParqueadero crearSesion(String estado) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("ABC123");
        Usuario usuario = new Usuario();
        usuario.setId(8);
        vehiculo.setUsuario(usuario);
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
