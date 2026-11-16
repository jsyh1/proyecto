package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.QrBeneficioDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.dao.ValidacionQrDAO;
import co.edu.poli.parqueadero.modelo.Beneficio;
import co.edu.poli.parqueadero.modelo.QrBeneficio;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.ValidacionQr;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ValidacionQrService {

    private static final Logger LOG = LoggerFactory.getLogger(ValidacionQrService.class);

    private final QrBeneficioDAO qrDAO;
    private final ValidacionQrDAO validacionDAO;
    private final SesionDAO sesionDAO;
    private final UsuarioDAO usuarioDAO;
    private final Clock reloj;

    public ValidacionQrService(
            QrBeneficioDAO qrDAO,
            ValidacionQrDAO validacionDAO,
            SesionDAO sesionDAO,
            UsuarioDAO usuarioDAO,
            Clock reloj) {
        this.qrDAO = qrDAO;
        this.validacionDAO = validacionDAO;
        this.sesionDAO = sesionDAO;
        this.usuarioDAO = usuarioDAO;
        this.reloj = reloj;
    }

    @Transactional
    public ResultadoValidacionQr validar(String correo, Integer idSesion, String codigoIngresado) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        SesionParqueadero sesion = sesionDAO.buscarPorIdYUsuario(
                idSesion, usuario == null ? -1 : usuario.getId());
        if (sesion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La sesión no existe o no pertenece al usuario.");
        }
        if (!SesionParqueadero.ESTADO_ABIERTA.equals(sesion.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se aceptan beneficios en sesiones abiertas.");
        }
        LocalDateTime ahora = LocalDateTime.now(reloj);
        String codigo = codigoIngresado == null ? "" : codigoIngresado.trim();
        QrBeneficio qr = codigo.isEmpty() ? null
                : qrDAO.buscarPorCodigoConBloqueo(BeneficioService.hashCodigo(codigo));
        String motivo = motivoRechazo(qr, sesion, ahora);
        ValidacionQr validacion = new ValidacionQr();
        validacion.setUsuario(usuario);
        validacion.setSesion(sesion);
        validacion.setQr(qr);
        validacion.setFechaValidacion(ahora);
        if (motivo != null) {
            validacion.setResultado(ValidacionQr.RECHAZADA);
            validacion.setMotivo(motivo);
            validacion.setDescuentoAplicado(BigDecimal.ZERO);
            validacionDAO.crear(validacion);
            LOG.info("Validación QR rechazada para la sesión {}: {}", idSesion, motivo);
            return new ResultadoValidacionQr(false, motivo, null, null, null);
        }

        Beneficio beneficio = qr.getBeneficio();
        if (Beneficio.HORAS_GRATIS.equals(beneficio.getTipo())) {
            BigDecimal horasActuales = sesion.getHorasGratuitas() == null
                    ? BigDecimal.ZERO : sesion.getHorasGratuitas();
            sesion.setHorasGratuitas(horasActuales.add(beneficio.getValor()));
            sesionDAO.actualizar(sesion);
        }
        qr.setConsumido(true);
        qr.setFechaConsumo(ahora);
        qrDAO.actualizar(qr);
        validacion.setResultado(ValidacionQr.APROBADA);
        validacion.setMotivo("Beneficio aplicado correctamente.");
        validacion.setDescuentoAplicado(BigDecimal.ZERO);
        validacionDAO.crear(validacion);
        LOG.info("Beneficio QR aplicado a la sesión {}", idSesion);
        return new ResultadoValidacionQr(true, "Beneficio aplicado correctamente.",
                beneficio.getNombre(), beneficio.getTipo(), beneficio.getValor());
    }

    @Transactional(readOnly = true)
    public List<ValidacionQrResponse> historial(String correo) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe.");
        }
        return validacionDAO.listarPorUsuario(usuario.getId()).stream()
                .map(ValidacionQrResponse::desde).toList();
    }

    private static String motivoRechazo(QrBeneficio qr, SesionParqueadero sesion, LocalDateTime ahora) {
        if (qr == null) {
            return "El código QR no es válido.";
        }
        if (qr.isConsumido()) {
            return "El código QR ya fue utilizado.";
        }
        Beneficio beneficio = qr.getBeneficio();
        LocalDate hoy = ahora.toLocalDate();
        if (!beneficio.isActivo() || hoy.isBefore(beneficio.getFechaInicio())
                || (beneficio.getFechaFin() != null && hoy.isAfter(beneficio.getFechaFin()))
                || !ahora.isBefore(qr.getFechaVencimiento())) {
            return "El beneficio o el código QR están vencidos o inactivos.";
        }
        if (!beneficio.getCentro().getId().equals(sesion.getCentro().getId())) {
            return "El beneficio pertenece a otro centro comercial.";
        }
        return null;
    }
}
