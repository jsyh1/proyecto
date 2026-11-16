package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.dao.PagoDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PagoServiceTest {

    private PagoDAO pagoDAO;
    private SesionDAO sesionDAO;
    private UsuarioDAO usuarioDAO;
    private CobroService cobroService;
    private PagoService servicio;
    private SesionParqueadero sesion;

    @BeforeEach
    void setUp() {
        pagoDAO = org.mockito.Mockito.mock(PagoDAO.class);
        sesionDAO = org.mockito.Mockito.mock(SesionDAO.class);
        usuarioDAO = org.mockito.Mockito.mock(UsuarioDAO.class);
        cobroService = org.mockito.Mockito.mock(CobroService.class);
        servicio = new PagoService(pagoDAO, sesionDAO, usuarioDAO, cobroService,
                Clock.fixed(Instant.parse("2026-11-15T17:00:00Z"), ZoneId.of("America/Bogota")));
        Usuario usuario = new Usuario();
        usuario.setId(7);
        usuario.setCorreo("ana@example.com");
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setUsuario(usuario);
        vehiculo.setPlaca("ABC123");
        sesion = new SesionParqueadero();
        sesion.setId(9);
        sesion.setVehiculo(vehiculo);
        sesion.setCentro(new CentroComercial());
        sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);
        sesion.setFechaEntrada(LocalDateTime.of(2026, 11, 15, 8, 0));
        when(usuarioDAO.buscarPorCorreo("ana@example.com")).thenReturn(usuario);
        when(sesionDAO.buscarPorIdParaActualizar(9)).thenReturn(sesion);
        when(pagoDAO.buscarPagoAprobado(9)).thenReturn(null);
        when(cobroService.calcular(sesion)).thenReturn(new ResumenCobro(
                9, "ABC123", "Centro Demo", "ABIERTA",
                new BigDecimal("10000.00"), new BigDecimal("2500.00"),
                new BigDecimal("7500.00"), java.util.List.of("Descuento demo")));
    }

    @Test
    void unPagoAprobadoRegistraMontoYCierraLaSesion() {
        ResultadoPago resultado = servicio.pagar("ana@example.com", 9, "SIMULADO_APROBADO");

        assertTrue(resultado.aprobado());
        assertEquals(SesionParqueadero.ESTADO_CERRADA, sesion.getEstado());
        assertEquals(new BigDecimal("7500.00"), sesion.getMontoTotal());
        verify(pagoDAO).crear(org.mockito.ArgumentMatchers.argThat(
                pago -> "APROBADO".equals(pago.getEstado()) && new BigDecimal("7500.00").equals(pago.getMonto())));
        verify(sesionDAO).actualizar(sesion);
    }

    @Test
    void unPagoRechazadoNoSeRegistraNiCierraLaSesion() {
        ResultadoPago resultado = servicio.pagar("ana@example.com", 9, "SIMULADO_RECHAZADO");

        assertFalse(resultado.aprobado());
        assertEquals(SesionParqueadero.ESTADO_ABIERTA, sesion.getEstado());
        verify(pagoDAO, never()).crear(org.mockito.ArgumentMatchers.any());
        verify(sesionDAO, never()).actualizar(sesion);
    }
}
