package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.dao.ValidacionQrDAO;
import co.edu.poli.parqueadero.modelo.Beneficio;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.ValidacionQr;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CobroService {

    private static final Logger LOG = LoggerFactory.getLogger(CobroService.class);

    private final SesionDAO sesionDAO;
    private final UsuarioDAO usuarioDAO;
    private final ValidacionQrDAO validacionDAO;
    private final SesionService sesionService;

    public CobroService(
            SesionDAO sesionDAO,
            UsuarioDAO usuarioDAO,
            ValidacionQrDAO validacionDAO,
            SesionService sesionService) {
        this.sesionDAO = sesionDAO;
        this.usuarioDAO = usuarioDAO;
        this.validacionDAO = validacionDAO;
        this.sesionService = sesionService;
    }

    @Transactional(readOnly = true)
    public ResumenCobro consultar(String correo, Integer idSesion) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        SesionParqueadero sesion = sesionDAO.buscarPorIdYUsuario(
                idSesion, usuario == null ? -1 : usuario.getId());
        if (sesion == null) {
            LOG.warn("No se pudo consultar el cobro de la sesión {}", idSesion);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La sesión no existe o no pertenece al usuario.");
        }
        LOG.info("Resumen de cobro consultado para sesión {}", idSesion);
        return calcular(sesion);
    }

    @Transactional
    public ResumenCobro calcular(SesionParqueadero sesion) {
        try {
            return calcularResumen(sesion);
        } catch (ArithmeticException | DataAccessException error) {
            LOG.error("No se pudo calcular el cobro de la sesión {}", sesion.getId(), error);
            throw error;
        }
    }

    private ResumenCobro calcularResumen(SesionParqueadero sesion) {
        ResumenSesion resumen = sesionService.consultarSesion(sesion.getId());
        BigDecimal costoInicial = SesionService.calcularCosto(
                resumen.minutosTranscurridos(), BigDecimal.ZERO, resumen.valorHora());
        BigDecimal costoConHorasGratis = resumen.costoActual();
        BigDecimal descuentoMonetario = BigDecimal.ZERO;
        List<String> nombres = new ArrayList<>();
        for (ValidacionQr validacion : validacionDAO.listarAprobadasPorSesion(sesion.getId())) {
            Beneficio beneficio = validacion.getQr().getBeneficio();
            nombres.add(beneficio.getNombre());
            if (Beneficio.PORCENTAJE_DESCUENTO.equals(beneficio.getTipo())) {
                descuentoMonetario = descuentoMonetario.add(
                        costoConHorasGratis.multiply(beneficio.getValor())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            } else if (Beneficio.VALOR_FIJO_DESCUENTO.equals(beneficio.getTipo())) {
                descuentoMonetario = descuentoMonetario.add(beneficio.getValor());
            }
        }
        descuentoMonetario = descuentoMonetario.min(costoConHorasGratis);
        BigDecimal valorFinal = costoConHorasGratis.subtract(descuentoMonetario).max(BigDecimal.ZERO);
        BigDecimal descuentoTotal = costoInicial.subtract(valorFinal).max(BigDecimal.ZERO);
        return new ResumenCobro(sesion.getId(), sesion.getVehiculo().getPlaca(),
                sesion.getCentro().getNombre(), sesion.getEstado(),
                costoInicial.setScale(2, RoundingMode.HALF_UP),
                descuentoTotal.setScale(2, RoundingMode.HALF_UP),
                valorFinal.setScale(2, RoundingMode.HALF_UP),
                List.copyOf(nombres));
    }
}
