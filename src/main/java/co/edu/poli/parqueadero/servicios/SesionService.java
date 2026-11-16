package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.RegistroAccesoDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.dao.VehiculoDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.RegistroAcceso;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Tarifa;
import co.edu.poli.parqueadero.modelo.Vehiculo;

/**
 * HU: registrar la entrada de un vehículo por su placa, iniciar la sesión de
 * parqueadero y consultar el tiempo y el costo de permanencia.
 */
@Service
public class SesionService {

    private static final BigDecimal MINUTOS_POR_HORA = BigDecimal.valueOf(60);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VehiculoDAO vehiculoDAO;
    private final SesionDAO sesionDAO;
    private final TarifaDAO tarifaDAO;
    private final CentroComercialDAO centroDAO;
    private final RegistroAccesoDAO accesoDAO;
    private final NotificacionService notificacionService;
    private final Clock reloj;

    public SesionService(VehiculoDAO vehiculoDAO, SesionDAO sesionDAO, TarifaDAO tarifaDAO,
            CentroComercialDAO centroDAO, RegistroAccesoDAO accesoDAO,
            NotificacionService notificacionService, Clock reloj) {
        this.vehiculoDAO = vehiculoDAO;
        this.sesionDAO = sesionDAO;
        this.tarifaDAO = tarifaDAO;
        this.centroDAO = centroDAO;
        this.accesoDAO = accesoDAO;
        this.notificacionService = notificacionService;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<CentroComercial> listarCentrosActivos() {
        return centroDAO.listarActivos();
    }

    /**
     * Registra la entrada de un vehículo. Las detecciones rechazadas también se
     * guardan en REGISTRO_ACCESO, por eso la transacción no se revierte con la excepción.
     */
    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public SesionParqueadero registrarEntrada(String placaIngresada, Integer idCentro) {
        String placa = ReglasPlaca.normalizar(placaIngresada);
        if (!ReglasPlaca.esValida(placa)) {
            throw new ReglaNegocioException("La placa ingresada no es válida.");
        }
        CentroComercial centro = idCentro == null ? null : centroDAO.buscarPorId(idCentro);
        if (centro == null) {
            throw new ReglaNegocioException("El centro comercial seleccionado no existe.");
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);

        Vehiculo vehiculo = vehiculoDAO.buscarPorPlaca(placa);
        if (vehiculo == null) {
            registrarAcceso(centro, placa, null, ahora, RegistroAcceso.RESULTADO_FALLIDO);
            throw new ReglaNegocioException("La placa " + placa + " no está registrada. Registra tu vehículo primero.");
        }
        if (sesionDAO.buscarAbiertaPorVehiculo(vehiculo.getId()) != null) {
            registrarAcceso(centro, placa, null, ahora, RegistroAcceso.RESULTADO_FALLIDO);
            throw new ReglaNegocioException("El vehículo " + placa + " ya tiene una sesión de parqueadero abierta.");
        }
        Tarifa tarifa = tarifaDAO.buscarVigente(centro.getId(), ahora.toLocalDate());
        if (tarifa == null) {
            registrarAcceso(centro, placa, null, ahora, RegistroAcceso.RESULTADO_FALLIDO);
            throw new ReglaNegocioException("El centro comercial no tiene una tarifa vigente.");
        }

        SesionParqueadero sesion = new SesionParqueadero();
        sesion.setVehiculo(vehiculo);
        sesion.setCentro(centro);
        sesion.setTarifa(tarifa);
        sesion.setFechaEntrada(ahora);
        sesion.setHorasGratuitas(BigDecimal.ZERO);
        sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);
        sesionDAO.crear(sesion);

        registrarAcceso(centro, placa, sesion, ahora, RegistroAcceso.RESULTADO_RECONOCIDO);
        notificacionService.entradaRegistrada(sesion);
        return sesion;
    }

    @Transactional(noRollbackFor = ReglaNegocioException.class)
    public SesionParqueadero registrarEntrada(String placaIngresada, Integer idCentro, String correoPropietario) {
        String placa = ReglasPlaca.normalizar(placaIngresada);
        Vehiculo vehiculo = vehiculoDAO.buscarPorPlaca(placa);
        if (vehiculo == null || vehiculo.getUsuario() == null
                || !vehiculo.getUsuario().getCorreo().equalsIgnoreCase(correoPropietario)) {
            throw new ReglaNegocioException("La placa no está registrada para la cuenta autenticada.");
        }
        return registrarEntrada(placa, idCentro);
    }

    @Transactional
    public ResumenSesion consultarSesion(Integer id) {
        SesionParqueadero sesion = id == null ? null : sesionDAO.buscarPorId(id);
        if (sesion == null) {
            throw new ReglaNegocioException("La sesión solicitada no existe.");
        }
        return resumir(sesion);
    }

    @Transactional
    public ResumenSesion consultarSesion(Integer id, String correoPropietario) {
        SesionParqueadero sesion = id == null ? null : sesionDAO.buscarPorId(id);
        if (sesion == null || sesion.getVehiculo().getUsuario() == null
                || !sesion.getVehiculo().getUsuario().getCorreo().equalsIgnoreCase(correoPropietario)) {
            throw new ReglaNegocioException("La sesión solicitada no existe para el usuario autenticado.");
        }
        return resumir(sesion);
    }

    @Transactional
    public ResumenSesion consultarSesionAbiertaPorPlaca(String placaIngresada) {
        String placa = ReglasPlaca.normalizar(placaIngresada);
        SesionParqueadero sesion = sesionDAO.buscarAbiertaPorPlaca(placa);
        if (sesion == null) {
            throw new ReglaNegocioException("No hay una sesión abierta para la placa " + placa + ".");
        }
        return resumir(sesion);
    }

    @Transactional
    public ResumenSesion consultarSesionAbiertaPorPlaca(String placaIngresada, String correoPropietario) {
        String placa = ReglasPlaca.normalizar(placaIngresada);
        SesionParqueadero sesion = sesionDAO.buscarAbiertaPorPlaca(placa);
        if (sesion == null || sesion.getVehiculo().getUsuario() == null
                || !sesion.getVehiculo().getUsuario().getCorreo().equalsIgnoreCase(correoPropietario)) {
            throw new ReglaNegocioException("No existe una sesión abierta para ese vehículo.");
        }
        return resumir(sesion);
    }

    private ResumenSesion resumir(SesionParqueadero sesion) {
        LocalDateTime hasta = sesion.getFechaSalida() != null ? sesion.getFechaSalida() : LocalDateTime.now(reloj);
        long minutos = Math.max(0, Duration.between(sesion.getFechaEntrada(), hasta).toMinutes());
        BigDecimal valorHora = sesion.getTarifa().getValorHora();
        BigDecimal gratuitas = sesion.getHorasGratuitas() != null ? sesion.getHorasGratuitas() : BigDecimal.ZERO;
        boolean abierta = SesionParqueadero.ESTADO_ABIERTA.equals(sesion.getEstado());
        BigDecimal montoGuardado = sesion.getMontoTotal();
        BigDecimal costo = !abierta && montoGuardado != null
                ? montoGuardado
                : calcularCosto(minutos, gratuitas, valorHora);
        if (abierta || montoGuardado == null) {
            sesion.setMontoTotal(costo);
        }
        return new ResumenSesion(
                sesion.getId(),
                sesion.getVehiculo().getPlaca(),
                sesion.getCentro().getNombre(),
                sesion.getFechaEntrada().format(FORMATO_FECHA),
                sesion.getEstado(),
                minutos,
                formatearDuracion(minutos),
                valorHora,
                gratuitas,
                costo);
    }

    private void registrarAcceso(CentroComercial centro, String placa, SesionParqueadero sesion,
            LocalDateTime fechaHora, String resultado) {
        RegistroAcceso acceso = new RegistroAcceso();
        acceso.setCentro(centro);
        acceso.setSesion(sesion);
        acceso.setPlacaDetectada(placa);
        acceso.setFechaHora(fechaHora);
        acceso.setTipoEvento(RegistroAcceso.EVENTO_ENTRADA);
        acceso.setResultadoDeteccion(resultado);
        accesoDAO.crear(acceso);
    }

    /** Quita espacios y guiones y pasa a mayúsculas: " abc-123 " queda "ABC123". */
    static String normalizarPlaca(String placa) {
        return ReglasPlaca.normalizar(placa);
    }

    /**
     * Regla de cobro: proporcional al minuto, sobre el valor de la hora de la tarifa
     * aplicada, descontando las horas gratuitas de la sesión. Es el único punto a
     * modificar si se decide cobrar por fracción u hora completa.
     */
    static BigDecimal calcularCosto(long minutosTranscurridos, BigDecimal horasGratuitas, BigDecimal valorHora) {
        BigDecimal minutosFacturables = BigDecimal.valueOf(minutosTranscurridos)
                .subtract(horasGratuitas.multiply(MINUTOS_POR_HORA))
                .max(BigDecimal.ZERO);
        return valorHora.multiply(minutosFacturables).divide(MINUTOS_POR_HORA, 2, RoundingMode.HALF_UP);
    }

    static String formatearDuracion(long minutos) {
        long horas = minutos / 60;
        long resto = minutos % 60;
        return horas == 0 ? resto + " min" : horas + " h " + resto + " min";
    }
}
