package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.PagoDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Pago;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Usuario;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PagoService {

    private static final Logger LOG = LoggerFactory.getLogger(PagoService.class);
    private static final Set<String> METODOS = Set.of("SIMULADO_APROBADO", "SIMULADO_RECHAZADO");

    private final PagoDAO pagoDAO;
    private final SesionDAO sesionDAO;
    private final UsuarioDAO usuarioDAO;
    private final CobroService cobroService;
    private final Clock reloj;

    public PagoService(
            PagoDAO pagoDAO,
            SesionDAO sesionDAO,
            UsuarioDAO usuarioDAO,
            CobroService cobroService,
            Clock reloj) {
        this.pagoDAO = pagoDAO;
        this.sesionDAO = sesionDAO;
        this.usuarioDAO = usuarioDAO;
        this.cobroService = cobroService;
        this.reloj = reloj;
    }

    @Transactional
    public ResultadoPago pagar(String correo, Integer idSesion, String metodoSolicitado) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        SesionParqueadero sesion = sesionDAO.buscarPorIdParaActualizar(idSesion);
        if (sesion == null || usuario == null || !sesion.getVehiculo().getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La sesión no existe o no pertenece al usuario.");
        }
        if (!SesionParqueadero.ESTADO_ABIERTA.equals(sesion.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión ya está cerrada.");
        }
        if (pagoDAO.buscarPagoAprobado(idSesion) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La sesión ya tiene un pago aprobado.");
        }
        String metodo = metodoSolicitado == null ? "" : metodoSolicitado.trim().toUpperCase();
        if (!METODOS.contains(metodo)) {
            LOG.warn("Método de pago simulado no válido para sesión {}", idSesion);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selecciona un método de pago de prueba válido.");
        }

        ResumenCobro cobro = cobroService.calcular(sesion);
        if ("SIMULADO_RECHAZADO".equals(metodo)) {
            LOG.info("Pago de prueba rechazado para sesión {}", idSesion);
            return new ResultadoPago(false, "El pago de prueba fue rechazado; no se registró ningún pago.",
                    idSesion, cobro.costoInicial(), cobro.descuentoBeneficios(), cobro.valorFinal());
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        Pago pago = new Pago();
        pago.setSesion(sesion);
        pago.setMetodo("SIMULADO");
        pago.setEstado("APROBADO");
        pago.setMonto(cobro.valorFinal());
        pago.setFechaPago(ahora);
        try {
            pagoDAO.crear(pago);
            sesion.setFechaSalida(ahora);
            sesion.setMontoTotal(cobro.valorFinal());
            sesion.setEstado(SesionParqueadero.ESTADO_CERRADA);
            BigDecimal minutosGratis = (sesion.getHorasGratuitas() == null
                    ? BigDecimal.ZERO : sesion.getHorasGratuitas()).multiply(BigDecimal.valueOf(60));
            long minutos = Math.max(0, java.time.Duration.between(sesion.getFechaEntrada(), ahora).toMinutes());
            sesion.setHorasFacturables(BigDecimal.valueOf(minutos).subtract(minutosGratis)
                    .max(BigDecimal.ZERO).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP));
            sesionDAO.actualizar(sesion);
        } catch (DataAccessException error) {
            LOG.error("No se pudo registrar el pago de la sesión {}", idSesion, error);
            throw error;
        }
        LOG.info("Pago simulado aprobado para sesión {}", idSesion);
        return new ResultadoPago(true, "Pago de prueba aprobado y sesión cerrada.",
                idSesion, cobro.costoInicial(), cobro.descuentoBeneficios(), cobro.valorFinal());
    }
}
