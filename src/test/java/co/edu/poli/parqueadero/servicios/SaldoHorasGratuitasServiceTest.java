package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.dao.CuentaAhorroDAO;
import co.edu.poli.parqueadero.dao.MovimientoAhorroDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.CuentaAhorro;
import co.edu.poli.parqueadero.modelo.MovimientoAhorro;
import co.edu.poli.parqueadero.modelo.Usuario;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class SaldoHorasGratuitasServiceTest {

    private CuentaAhorroDAO cuentaDAO;
    private MovimientoAhorroDAO movimientoDAO;
    private UsuarioDAO usuarioDAO;
    private SaldoHorasGratuitasService servicio;
    private CuentaAhorro cuenta;

    @BeforeEach
    void setUp() {
        cuentaDAO = mock(CuentaAhorroDAO.class);
        movimientoDAO = mock(MovimientoAhorroDAO.class);
        usuarioDAO = mock(UsuarioDAO.class);
        servicio = new SaldoHorasGratuitasService(
                cuentaDAO,
                movimientoDAO,
                usuarioDAO,
                Clock.fixed(Instant.parse("2026-11-19T15:00:00Z"), ZoneId.of("America/Bogota")));
        cuenta = new CuentaAhorro();
        cuenta.setSaldoHoras(BigDecimal.ZERO);
        when(cuentaDAO.buscarOCrearBloqueada(8)).thenReturn(cuenta);
    }

    @Test
    void reservaElSaldoDisponibleSinPermitirSaldoNegativo() {
        cuenta.setSaldoHoras(new BigDecimal("2.500000"));

        BigDecimal reservado = servicio.reservarSaldoDisponible(8, 41);

        assertEquals(new BigDecimal("2.500000"), reservado);
        assertEquals(BigDecimal.ZERO, cuenta.getSaldoHoras());
        ArgumentCaptor<MovimientoAhorro> captor = ArgumentCaptor.forClass(MovimientoAhorro.class);
        verify(movimientoDAO).crear(captor.capture());
        assertEquals(new BigDecimal("-2.500000"), captor.getValue().getCantidadHoras());
        assertEquals("Saldo reservado para la sesión 41.", captor.getValue().getMotivo());
        assertEquals(LocalDateTime.of(2026, 11, 19, 10, 0), captor.getValue().getFechaHora());
    }

    @Test
    void noRegistraDebitosCuandoElSaldoEstaEnCero() {
        BigDecimal reservado = servicio.reservarSaldoDisponible(8, 41);

        assertEquals(BigDecimal.ZERO, reservado);
        assertEquals(BigDecimal.ZERO, cuenta.getSaldoHoras());
        verify(movimientoDAO, never()).crear(any(MovimientoAhorro.class));
    }

    @Test
    void acreditaYReservaUnaHoraPorCompraValidada() {
        BigDecimal reservadas = servicio.acreditarCompraYReservar(8, 41, BigDecimal.ONE);

        assertEquals(BigDecimal.ONE, reservadas);
        assertEquals(BigDecimal.ZERO, cuenta.getSaldoHoras());
        ArgumentCaptor<MovimientoAhorro> captor = ArgumentCaptor.forClass(MovimientoAhorro.class);
        verify(movimientoDAO, org.mockito.Mockito.times(2)).crear(captor.capture());
        assertEquals(BigDecimal.ONE, captor.getAllValues().get(0).getCantidadHoras());
        assertEquals(new BigDecimal("-1"), captor.getAllValues().get(1).getCantidadHoras());
    }

    @Test
    void liberaLaReservaYDescuentaSoloLasHorasUtilizadas() {
        cuenta.setSaldoHoras(BigDecimal.ZERO);

        servicio.finalizarUsoEnSesion(
                8, 41, new BigDecimal("2.000000"), new BigDecimal("0.250000"));

        assertEquals(new BigDecimal("1.750000"), cuenta.getSaldoHoras());
        ArgumentCaptor<MovimientoAhorro> captor = ArgumentCaptor.forClass(MovimientoAhorro.class);
        verify(movimientoDAO, org.mockito.Mockito.times(2)).crear(captor.capture());
        assertEquals(new BigDecimal("2.000000"), captor.getAllValues().get(0).getCantidadHoras());
        assertEquals(new BigDecimal("-0.250000"), captor.getAllValues().get(1).getCantidadHoras());
    }

    @Test
    void rechazaConsumoMayorQueLaReserva() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.finalizarUsoEnSesion(8, 41, BigDecimal.ONE, new BigDecimal("1.5")));

        verify(cuentaDAO, never()).buscarOCrearBloqueada(8);
        verify(movimientoDAO, never()).crear(any(MovimientoAhorro.class));
    }

    @Test
    void consultaSaldoYMovimientosDeUsuarioExistente() {
        Usuario usuario = new Usuario();
        when(usuarioDAO.buscarPorId(8)).thenReturn(usuario);
        cuenta.setSaldoHoras(new BigDecimal("1.250000"));
        MovimientoAhorro movimiento = new MovimientoAhorro();
        movimiento.setFechaHora(LocalDateTime.of(2026, 11, 19, 10, 0));
        movimiento.setCantidadHoras(BigDecimal.ONE);
        movimiento.setMotivo("Compra validada");
        when(cuentaDAO.buscarPorUsuario(8)).thenReturn(cuenta);
        when(movimientoDAO.listarPorUsuario(8)).thenReturn(java.util.List.of(movimiento));

        SaldoHorasGratuitasResponse respuesta = servicio.consultar(8);

        assertEquals(8, respuesta.idUsuario());
        assertEquals(new BigDecimal("1.250000"), respuesta.saldoHoras());
        assertEquals(1, respuesta.movimientos().size());
        assertEquals(LocalDateTime.of(2026, 11, 19, 10, 0), respuesta.movimientos().get(0).fechaHora());
        assertEquals(BigDecimal.ONE, respuesta.movimientos().get(0).cantidadHoras());
        assertEquals("Compra validada", respuesta.movimientos().get(0).motivo());
    }

    @Test
    void noPermiteConsultarSaldoDeUsuarioInexistente() {
        when(usuarioDAO.buscarPorId(8)).thenReturn(null);

        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
                () -> servicio.consultar(8));

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatusCode());
        verify(cuentaDAO, never()).buscarPorUsuario(8);
    }
}
