package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.BeneficioDAO;
import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.QrBeneficioDAO;
import co.edu.poli.parqueadero.modelo.Beneficio;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.QrBeneficio;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BeneficioService {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final BeneficioDAO beneficioDAO;
    private final CentroComercialDAO centroDAO;
    private final QrBeneficioDAO qrDAO;

    public BeneficioService(BeneficioDAO beneficioDAO, CentroComercialDAO centroDAO, QrBeneficioDAO qrDAO) {
        this.beneficioDAO = beneficioDAO;
        this.centroDAO = centroDAO;
        this.qrDAO = qrDAO;
    }

    @Transactional(readOnly = true)
    public List<BeneficioResponse> listar(Integer idCentro) {
        return beneficioDAO.listarPorCentro(idCentro).stream().map(BeneficioResponse::desde).toList();
    }

    @Transactional
    public BeneficioResponse crear(SolicitudBeneficio solicitud) {
        Beneficio beneficio = new Beneficio();
        aplicar(beneficio, solicitud);
        beneficioDAO.crear(beneficio);
        return BeneficioResponse.desde(beneficio);
    }

    @Transactional
    public BeneficioResponse actualizar(Integer id, SolicitudBeneficio solicitud) {
        Beneficio beneficio = beneficioDAO.buscarPorId(id);
        if (beneficio == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El beneficio no existe.");
        }
        aplicar(beneficio, solicitud);
        beneficioDAO.actualizar(beneficio);
        return BeneficioResponse.desde(beneficio);
    }

    @Transactional
    public List<String> emitirCodigos(Integer idBeneficio, int cantidad, LocalDateTime vencimiento) {
        if (cantidad < 1 || cantidad > 100 || vencimiento == null || !vencimiento.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica entre 1 y 100 códigos con fecha de vencimiento futura.");
        }
        Beneficio beneficio = beneficioDAO.buscarPorId(idBeneficio);
        if (beneficio == null || !beneficio.isActivo()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El beneficio activo no existe.");
        }
        List<String> codigos = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            QrBeneficio qr = new QrBeneficio();
            String codigo = nuevoCodigo();
            qr.setCodigoHash(hashCodigo(codigo));
            qr.setBeneficio(beneficio);
            qr.setFechaVencimiento(vencimiento);
            qr.setConsumido(false);
            qrDAO.crear(qr);
            codigos.add(codigo);
        }
        return codigos;
    }

    private void aplicar(Beneficio beneficio, SolicitudBeneficio solicitud) {
        CentroComercial centro = centroDAO.buscarPorId(solicitud.idCentro());
        if (centro == null || !CentroComercial.ESTADO_ACTIVO.equalsIgnoreCase(centro.getEstado())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El centro comercial activo no existe.");
        }
        String tipo = solicitud.tipo() == null ? "" : solicitud.tipo().trim().toUpperCase();
        BigDecimal valor = solicitud.valor();
        if (solicitud.nombre() == null || solicitud.nombre().isBlank()
                || !List.of(Beneficio.HORAS_GRATIS, Beneficio.PORCENTAJE_DESCUENTO,
                        Beneficio.VALOR_FIJO_DESCUENTO).contains(tipo)
                || valor == null || valor.signum() <= 0
                || (Beneficio.PORCENTAJE_DESCUENTO.equals(tipo)
                        && valor.compareTo(BigDecimal.valueOf(100)) > 0)
                || solicitud.fechaInicio() == null
                || (solicitud.fechaFin() != null && solicitud.fechaFin().isBefore(solicitud.fechaInicio()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del beneficio no son válidos.");
        }
        beneficio.setCentro(centro);
        beneficio.setNombre(solicitud.nombre().trim());
        beneficio.setTipo(tipo);
        beneficio.setValor(valor);
        beneficio.setFechaInicio(solicitud.fechaInicio());
        beneficio.setFechaFin(solicitud.fechaFin());
        beneficio.setActivo(solicitud.activo());
    }

    private static String nuevoCodigo() {
        byte[] bytes = new byte[24];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hashCodigo(String codigo) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(codigo.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en esta plataforma.", e);
        }
    }

    public record SolicitudBeneficio(
            Integer idCentro,
            String nombre,
            String tipo,
            BigDecimal valor,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            boolean activo) {
    }

    public record SolicitudCodigos(int cantidad, LocalDateTime vencimiento) {
    }
}
