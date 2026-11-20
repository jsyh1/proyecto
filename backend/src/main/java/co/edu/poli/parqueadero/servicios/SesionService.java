package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.poli.parqueadero.dao.BeneficioQrDAO;
import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.RegistroAccesoDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.dao.VehiculoDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.BeneficioQrAplicado;
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
    private static final BigDecimal HORAS_GRATUITAS_POR_COMPRA_VALIDA = BigDecimal.ONE;
    private static final int LONGITUD_MAXIMA_CLAVE_QR = 128;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VehiculoDAO vehiculoDAO;
    private final SesionDAO sesionDAO;
    private final BeneficioQrDAO beneficioQrDAO;
    private final TarifaDAO tarifaDAO;
    private final CentroComercialDAO centroDAO;
    private final RegistroAccesoDAO accesoDAO;
    private final SaldoHorasGratuitasService saldoHorasService;
    private final Clock reloj;

    public SesionService(VehiculoDAO vehiculoDAO, SesionDAO sesionDAO, BeneficioQrDAO beneficioQrDAO,
            TarifaDAO tarifaDAO,
            CentroComercialDAO centroDAO, RegistroAccesoDAO accesoDAO,
            SaldoHorasGratuitasService saldoHorasService, Clock reloj) {
        this.vehiculoDAO = vehiculoDAO;
        this.sesionDAO = sesionDAO;
        this.beneficioQrDAO = beneficioQrDAO;
        this.tarifaDAO = tarifaDAO;
        this.centroDAO = centroDAO;
        this.accesoDAO = accesoDAO;
        this.saldoHorasService = saldoHorasService;
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

        Vehiculo vehiculo = vehiculoDAO.buscarPorPlacaBloqueado(placa);
        if (vehiculo == null) {
            registrarAcceso(centro, placa, null, ahora, RegistroAcceso.EVENTO_ENTRADA,
                    RegistroAcceso.RESULTADO_FALLIDO);
            throw new ReglaNegocioException("La placa " + placa + " no está registrada. Registra tu vehículo primero.");
        }
        if (sesionDAO.buscarAbiertaPorVehiculo(vehiculo.getId()) != null) {
            registrarAcceso(centro, placa, null, ahora, RegistroAcceso.EVENTO_ENTRADA,
                    RegistroAcceso.RESULTADO_FALLIDO);
            throw new ReglaNegocioException("El vehículo " + placa + " ya tiene una sesión de parqueadero abierta.");
        }
        Tarifa tarifa = tarifaDAO.buscarVigente(centro.getId(), ahora.toLocalDate());
        if (tarifa == null) {
            registrarAcceso(centro, placa, null, ahora, RegistroAcceso.EVENTO_ENTRADA,
                    RegistroAcceso.RESULTADO_FALLIDO);
            throw new ReglaNegocioException("El centro comercial no tiene una tarifa vigente.");
        }

        SesionParqueadero sesion = new SesionParqueadero();
        sesion.setVehiculo(vehiculo);
        sesion.setCentro(centro);
        sesion.setTarifa(tarifa);
        sesion.setFechaEntrada(ahora);
        sesion.setHorasGratuitas(BigDecimal.ZERO);
        sesion.setBeneficiosQrAplicados(0);
        sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);
        sesionDAO.crear(sesion);
        BigDecimal horasReservadas = saldoHorasService.reservarSaldoDisponible(
                vehiculo.getUsuario().getId(), sesion.getId());
        sesion.setHorasGratuitas(horasReservadas);

        registrarAcceso(centro, placa, sesion, ahora, RegistroAcceso.EVENTO_ENTRADA,
                RegistroAcceso.RESULTADO_RECONOCIDO);
        return sesion;
    }

    @Transactional
    public ResumenSesion registrarSalidaPorPlaca(String placaIngresada) {
        String placa = ReglasPlaca.normalizar(placaIngresada);
        if (!ReglasPlaca.esValida(placa)) {
            throw new ReglaNegocioException("La placa ingresada no es válida.");
        }

        SesionParqueadero sesion = sesionDAO.buscarAbiertaPorPlacaBloqueada(placa);
        if (sesion == null) {
            throw new ReglaNegocioException("No hay una sesión abierta para la placa " + placa + ".");
        }
        if (sesion.getFechaEntrada() == null) {
            throw new ReglaNegocioException("La sesión no tiene una hora de entrada registrada.");
        }

        LocalDateTime fechaSalida = LocalDateTime.now(reloj);
        if (fechaSalida.isBefore(sesion.getFechaEntrada())) {
            throw new ReglaNegocioException("La hora de salida no puede ser anterior a la hora de entrada.");
        }

        long minutosEstacionamiento = Duration.between(sesion.getFechaEntrada(), fechaSalida).toMinutes();
        BigDecimal horasReservadas = sesion.getHorasGratuitas() != null
                ? sesion.getHorasGratuitas()
                : BigDecimal.ZERO;
        BigDecimal horasEstacionadas = BigDecimal.valueOf(minutosEstacionamiento)
                .divide(MINUTOS_POR_HORA, 6, RoundingMode.DOWN);
        BigDecimal horasGratuitas = horasReservadas.min(horasEstacionadas);
        if (horasReservadas.signum() > 0) {
            saldoHorasService.finalizarUsoEnSesion(
                    sesion.getVehiculo().getUsuario().getId(),
                    sesion.getId(),
                    horasReservadas,
                    horasGratuitas);
        }
        sesion.setHorasGratuitas(horasGratuitas);

        BigDecimal horasFacturables = calcularHorasFacturables(minutosEstacionamiento, horasGratuitas);
        BigDecimal costoFinal = calcularCosto(minutosEstacionamiento, horasGratuitas,
                sesion.getTarifa().getValorHora());

        sesion.setFechaSalida(fechaSalida);
        sesion.setHorasFacturables(horasFacturables);
        sesion.setMontoTotal(costoFinal);
        sesion.setEstado(SesionParqueadero.ESTADO_CERRADA);
        registrarAcceso(sesion.getCentro(), placa, sesion, fechaSalida, RegistroAcceso.EVENTO_SALIDA,
                RegistroAcceso.RESULTADO_RECONOCIDO);
        return resumir(sesion);
    }

    @Transactional
    public ResumenSesion consultarSesion(Integer id) {
        SesionParqueadero sesion = sesionDAO.buscarPorIdBloqueado(id);
        if (sesion == null) {
            throw new ReglaNegocioException("La sesión solicitada no existe.");
        }
        return resumir(sesion);
    }

    /**
     * Aplica el beneficio de una compra previamente validada por el flujo QR.
     */
    @Transactional
    public ResumenSesion aplicarBeneficioCompraValida(Integer idSesion, String claveCompraQr) {
        String claveCompraHash = hashClaveCompraQr(claveCompraQr);
        SesionParqueadero sesion = sesionDAO.buscarPorIdBloqueado(idSesion);
        if (sesion == null) {
            throw new ReglaNegocioException("La sesión solicitada no existe.");
        }
        BeneficioQrAplicado beneficioExistente = beneficioQrDAO.buscarPorClaveCompraHash(claveCompraHash);
        if (beneficioExistente != null) {
            if (!sesion.getId().equals(beneficioExistente.getSesion().getId())) {
                throw new ReglaNegocioException("La compra QR ya fue aplicada a otra sesión.");
            }
            return resumir(sesion);
        }
        if (!SesionParqueadero.ESTADO_ABIERTA.equals(sesion.getEstado())) {
            throw new ReglaNegocioException("No se pueden aplicar beneficios a una sesión cerrada.");
        }

        BigDecimal horasGratuitas = sesion.getHorasGratuitas() != null
                ? sesion.getHorasGratuitas()
                : BigDecimal.ZERO;
        if (sesion.getBeneficiosQrAplicados() < 0) {
            throw new ReglaNegocioException("La cantidad de beneficios QR no puede ser negativa.");
        }
        BeneficioQrAplicado beneficio = new BeneficioQrAplicado();
        beneficio.setClaveCompraHash(claveCompraHash);
        beneficio.setSesion(sesion);
        beneficio.setFechaAplicacion(LocalDateTime.now(reloj));
        beneficioQrDAO.crear(beneficio);

        BigDecimal horasAcreditadas = saldoHorasService.acreditarCompraYReservar(
                sesion.getVehiculo().getUsuario().getId(),
                sesion.getId(),
                HORAS_GRATUITAS_POR_COMPRA_VALIDA);
        sesion.setHorasGratuitas(horasGratuitas.add(horasAcreditadas));
        sesion.setBeneficiosQrAplicados(Math.addExact(sesion.getBeneficiosQrAplicados(), 1));
        return resumir(sesion);
    }

    @Transactional
    public ResumenSesion consultarSesionAbiertaPorPlaca(String placaIngresada) {
        String placa = ReglasPlaca.normalizar(placaIngresada);
        SesionParqueadero sesion = sesionDAO.buscarAbiertaPorPlacaBloqueada(placa);
        if (sesion == null) {
            throw new ReglaNegocioException("No hay una sesión abierta para la placa " + placa + ".");
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
        BigDecimal horasFacturables = sesion.getHorasFacturables() != null && !abierta
                ? sesion.getHorasFacturables()
                : calcularHorasFacturables(minutos, gratuitas);
        if (abierta || montoGuardado == null) {
            sesion.setMontoTotal(costo);
        }
        return new ResumenSesion(
                sesion.getId(),
                sesion.getVehiculo().getPlaca(),
                sesion.getCentro().getNombre(),
                sesion.getFechaEntrada().format(FORMATO_FECHA),
                sesion.getFechaSalida() != null ? sesion.getFechaSalida().format(FORMATO_FECHA) : null,
                sesion.getEstado(),
                minutos,
                formatearDuracion(minutos),
                valorHora,
                horasFacturables,
                gratuitas,
                sesion.getBeneficiosQrAplicados(),
                costo);
    }

    private void registrarAcceso(CentroComercial centro, String placa, SesionParqueadero sesion,
            LocalDateTime fechaHora, String tipoEvento, String resultado) {
        RegistroAcceso acceso = new RegistroAcceso();
        acceso.setCentro(centro);
        acceso.setSesion(sesion);
        acceso.setPlacaDetectada(placa);
        acceso.setFechaHora(fechaHora);
        acceso.setTipoEvento(tipoEvento);
        acceso.setResultadoDeteccion(resultado);
        accesoDAO.crear(acceso);
    }

    private static BigDecimal calcularHorasFacturables(long minutosEstacionamiento, BigDecimal horasGratuitas) {
        BigDecimal minutosFacturables = BigDecimal.valueOf(minutosEstacionamiento)
                .subtract(horasGratuitas.multiply(MINUTOS_POR_HORA))
                .max(BigDecimal.ZERO);
        return minutosFacturables.divide(MINUTOS_POR_HORA, 2, RoundingMode.HALF_UP);
    }

    private static String hashClaveCompraQr(String claveCompraQr) {
        if (claveCompraQr == null || claveCompraQr.isBlank() || claveCompraQr.length() > LONGITUD_MAXIMA_CLAVE_QR) {
            throw new ReglaNegocioException("La clave de la compra QR no es válida.");
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(claveCompraQr.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No está disponible SHA-256 para validar la compra QR.", e);
        }
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
