package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.CuentaAhorroDAO;
import co.edu.poli.parqueadero.dao.MovimientoAhorroDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.CuentaAhorro;
import co.edu.poli.parqueadero.modelo.MovimientoAhorro;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SaldoHorasGratuitasService {

    private final CuentaAhorroDAO cuentaDAO;
    private final MovimientoAhorroDAO movimientoDAO;
    private final UsuarioDAO usuarioDAO;
    private final Clock reloj;

    public SaldoHorasGratuitasService(
            CuentaAhorroDAO cuentaDAO,
            MovimientoAhorroDAO movimientoDAO,
            UsuarioDAO usuarioDAO,
            Clock reloj) {
        this.cuentaDAO = cuentaDAO;
        this.movimientoDAO = movimientoDAO;
        this.usuarioDAO = usuarioDAO;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public SaldoHorasGratuitasResponse consultar(Integer idUsuario) {
        validarUsuario(idUsuario);
        CuentaAhorro cuenta = cuentaDAO.buscarPorUsuario(idUsuario);
        BigDecimal saldo = cuenta == null ? BigDecimal.ZERO : saldo(cuenta);
        var movimientos = movimientoDAO.listarPorUsuario(idUsuario).stream()
                .map(movimiento -> new SaldoHorasGratuitasResponse.Movimiento(
                        movimiento.getFechaHora(),
                        movimiento.getCantidadHoras(),
                        movimiento.getMotivo()))
                .toList();
        return new SaldoHorasGratuitasResponse(idUsuario, saldo, movimientos);
    }

    @Transactional
    public BigDecimal reservarSaldoDisponible(Integer idUsuario, Integer idSesion) {
        CuentaAhorro cuenta = obtenerCuentaBloqueada(idUsuario);
        BigDecimal disponible = saldo(cuenta);
        if (disponible.signum() > 0) {
            mover(cuenta, disponible.negate(), "Saldo reservado para la sesión " + idSesion + ".");
            cuenta.setSaldoHoras(BigDecimal.ZERO);
        }
        return disponible;
    }

    @Transactional
    public BigDecimal acreditarCompraYReservar(Integer idUsuario, Integer idSesion, BigDecimal horas) {
        if (horas == null || horas.signum() <= 0) {
            throw new IllegalArgumentException("Las horas acreditadas deben ser positivas.");
        }
        CuentaAhorro cuenta = obtenerCuentaBloqueada(idUsuario);
        BigDecimal saldoActual = saldo(cuenta);
        mover(cuenta, horas, "Compra validada; beneficio acreditado.");
        mover(cuenta, horas.negate(), "Beneficio aplicado a la sesión " + idSesion + ".");
        cuenta.setSaldoHoras(saldoActual);
        return horas;
    }

    @Transactional
    public void finalizarUsoEnSesion(
            Integer idUsuario, Integer idSesion, BigDecimal horasReservadas, BigDecimal horasUtilizadas) {
        if (horasReservadas == null || horasReservadas.signum() < 0
                || horasUtilizadas == null || horasUtilizadas.signum() < 0
                || horasUtilizadas.compareTo(horasReservadas) > 0) {
            throw new IllegalArgumentException("Las horas utilizadas deben estar entre cero y las reservadas.");
        }
        if (horasReservadas.signum() == 0) {
            return;
        }
        CuentaAhorro cuenta = obtenerCuentaBloqueada(idUsuario);
        mover(cuenta, horasReservadas, "Reserva liberada al cerrar la sesión " + idSesion + ".");
        if (horasUtilizadas.signum() > 0) {
            mover(cuenta, horasUtilizadas.negate(), "Horas gratuitas utilizadas en la sesión " + idSesion + ".");
        }
        cuenta.setSaldoHoras(saldo(cuenta).add(horasReservadas).subtract(horasUtilizadas));
    }

    private CuentaAhorro obtenerCuentaBloqueada(Integer idUsuario) {
        CuentaAhorro cuenta = cuentaDAO.buscarOCrearBloqueada(idUsuario);
        if (cuenta == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario solicitado no existe.");
        }
        return cuenta;
    }

    private void validarUsuario(Integer idUsuario) {
        if (idUsuario == null || idUsuario <= 0 || usuarioDAO.buscarPorId(idUsuario) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario solicitado no existe.");
        }
    }

    private static BigDecimal saldo(CuentaAhorro cuenta) {
        BigDecimal saldo = cuenta.getSaldoHoras();
        if (saldo == null || saldo.signum() < 0) {
            throw new IllegalStateException("El saldo de horas gratuitas no puede ser negativo.");
        }
        return saldo;
    }

    private void mover(CuentaAhorro cuenta, BigDecimal cantidad, String motivo) {
        MovimientoAhorro movimiento = new MovimientoAhorro();
        movimiento.setCuenta(cuenta);
        movimiento.setFechaHora(LocalDateTime.now(reloj));
        movimiento.setCantidadHoras(cantidad);
        movimiento.setMotivo(motivo);
        movimientoDAO.crear(movimiento);
    }
}
