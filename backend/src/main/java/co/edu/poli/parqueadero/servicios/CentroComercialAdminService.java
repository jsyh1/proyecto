package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.Tarifa;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CentroComercialAdminService {

    private final CentroComercialDAO centroDAO;
    private final TarifaDAO tarifaDAO;
    private final Clock reloj;

    public CentroComercialAdminService(CentroComercialDAO centroDAO, TarifaDAO tarifaDAO, Clock reloj) {
        this.centroDAO = centroDAO;
        this.tarifaDAO = tarifaDAO;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<CentroComercial> listar() {
        return centroDAO.listar();
    }

    @Transactional
    public CentroComercial registrar(String nombre, String direccion, BigDecimal valorHora) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        String direccionLimpia = direccion == null ? "" : direccion.trim();
        if (nombreLimpio.isEmpty()) {
            throw new ReglaNegocioException("El nombre del centro comercial es obligatorio.");
        }
        if (nombreLimpio.length() > 80) {
            throw new ReglaNegocioException("El nombre no puede superar 80 caracteres.");
        }
        if (direccionLimpia.length() > 120) {
            throw new ReglaNegocioException("La dirección no puede superar 120 caracteres.");
        }
        if (valorHora == null || valorHora.signum() <= 0) {
            throw new ReglaNegocioException("La tarifa por hora debe ser mayor que cero.");
        }

        BigDecimal tarifaNormalizada;
        try {
            tarifaNormalizada = valorHora.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException excepcion) {
            throw new ReglaNegocioException("La tarifa solo puede tener dos decimales.");
        }
        if (tarifaNormalizada.precision() > 10) {
            throw new ReglaNegocioException("La tarifa supera el valor máximo permitido.");
        }

        CentroComercial centro = new CentroComercial();
        centro.setNombre(nombreLimpio);
        centro.setDireccion(direccionLimpia.isEmpty() ? null : direccionLimpia);
        centro.setEstado(CentroComercial.ESTADO_ACTIVO);
        centroDAO.crear(centro);

        Tarifa tarifa = new Tarifa();
        tarifa.setCentro(centro);
        tarifa.setValorHora(tarifaNormalizada);
        tarifa.setFechaInicio(LocalDate.now(reloj));
        tarifa.setEstado(Tarifa.ESTADO_ACTIVA);
        tarifaDAO.crear(tarifa);
        return centro;
    }
}
