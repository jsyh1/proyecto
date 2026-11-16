package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.Tarifa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TarifaService {

    private final TarifaDAO tarifaDAO;
    private final CentroComercialDAO centroDAO;

    public TarifaService(TarifaDAO tarifaDAO, CentroComercialDAO centroDAO) {
        this.tarifaDAO = tarifaDAO;
        this.centroDAO = centroDAO;
    }

    @Transactional(readOnly = true)
    public List<TarifaResponse> listar(Integer idCentro) {
        return tarifaDAO.listarPorCentro(idCentro).stream().map(TarifaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public TarifaResponse vigente(Integer idCentro) {
        Tarifa tarifa = tarifaDAO.buscarVigente(idCentro, LocalDate.now());
        if (tarifa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay una tarifa vigente para el centro.");
        }
        return TarifaResponse.desde(tarifa);
    }

    @Transactional
    public TarifaResponse crear(SolicitudTarifa solicitud) {
        Tarifa tarifa = new Tarifa();
        aplicar(tarifa, solicitud, null);
        tarifaDAO.crear(tarifa);
        return TarifaResponse.desde(tarifa);
    }

    @Transactional
    public TarifaResponse actualizar(Integer id, SolicitudTarifa solicitud) {
        Tarifa tarifa = tarifaDAO.buscarPorId(id);
        if (tarifa == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La tarifa no existe.");
        }
        aplicar(tarifa, solicitud, id);
        tarifaDAO.actualizar(tarifa);
        return TarifaResponse.desde(tarifa);
    }

    private void aplicar(Tarifa tarifa, SolicitudTarifa solicitud, Integer idActual) {
        CentroComercial centro = centroDAO.buscarPorId(solicitud.idCentro());
        if (centro == null || !CentroComercial.ESTADO_ACTIVO.equalsIgnoreCase(centro.getEstado())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El centro comercial activo no existe.");
        }
        if (solicitud.valorHora() == null || solicitud.valorHora().signum() <= 0
                || solicitud.fechaInicio() == null
                || (solicitud.fechaFin() != null && solicitud.fechaFin().isBefore(solicitud.fechaInicio()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos de la tarifa no son válidos.");
        }
        if (Tarifa.ESTADO_ACTIVA.equalsIgnoreCase(solicitud.estado())) {
            for (Tarifa existente : tarifaDAO.listarPorCentro(centro.getId())) {
                if (existente.getId().equals(idActual) || !Tarifa.ESTADO_ACTIVA.equalsIgnoreCase(existente.getEstado())) {
                    continue;
                }
                boolean traslapada = (solicitud.fechaFin() == null || !solicitud.fechaFin().isBefore(existente.getFechaInicio()))
                        && (existente.getFechaFin() == null || !existente.getFechaFin().isBefore(solicitud.fechaInicio()));
                if (traslapada) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "La vigencia se superpone con otra tarifa activa del centro.");
                }
            }
        }
        tarifa.setCentro(centro);
        tarifa.setValorHora(solicitud.valorHora());
        tarifa.setFechaInicio(solicitud.fechaInicio());
        tarifa.setFechaFin(solicitud.fechaFin());
        tarifa.setEstado(solicitud.estado() == null ? "" : solicitud.estado().trim().toUpperCase());
        if (!List.of(Tarifa.ESTADO_ACTIVA, "INACTIVA").contains(tarifa.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado debe ser ACTIVA o INACTIVA.");
        }
    }

    public record SolicitudTarifa(
            Integer idCentro,
            BigDecimal valorHora,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String estado) {
    }
}
