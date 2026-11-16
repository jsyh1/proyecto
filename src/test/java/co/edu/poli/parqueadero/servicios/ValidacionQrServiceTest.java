package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.dao.QrBeneficioDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.dao.ValidacionQrDAO;
import co.edu.poli.parqueadero.modelo.Beneficio;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.QrBeneficio;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.ValidacionQr;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidacionQrServiceTest {

    private QrBeneficioDAO qrDAO;
    private ValidacionQrDAO validacionDAO;
    private SesionDAO sesionDAO;
    private UsuarioDAO usuarioDAO;
    private ValidacionQrService servicio;
    private Usuario usuario;
    private SesionParqueadero sesion;
    private QrBeneficio qr;

    @BeforeEach
    void setUp() {
        qrDAO = org.mockito.Mockito.mock(QrBeneficioDAO.class);
        validacionDAO = org.mockito.Mockito.mock(ValidacionQrDAO.class);
        sesionDAO = org.mockito.Mockito.mock(SesionDAO.class);
        usuarioDAO = org.mockito.Mockito.mock(UsuarioDAO.class);
        servicio = new ValidacionQrService(qrDAO, validacionDAO, sesionDAO, usuarioDAO,
                Clock.fixed(Instant.parse("2026-11-15T17:00:00Z"), ZoneId.of("America/Bogota")));

        usuario = new Usuario();
        usuario.setId(7);
        usuario.setCorreo("ana@example.com");
        CentroComercial centro = new CentroComercial();
        centro.setId(3);
        centro.setNombre("Centro Demo");
        sesion = new SesionParqueadero();
        sesion.setId(9);
        sesion.setCentro(centro);
        sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);
        sesion.setHorasGratuitas(BigDecimal.ZERO);

        Beneficio beneficio = new Beneficio();
        beneficio.setId(11);
        beneficio.setCentro(centro);
        beneficio.setNombre("Una hora gratis");
        beneficio.setTipo(Beneficio.HORAS_GRATIS);
        beneficio.setValor(BigDecimal.ONE);
        beneficio.setFechaInicio(LocalDate.of(2026, 1, 1));
        beneficio.setActivo(true);

        qr = new QrBeneficio();
        qr.setCodigoHash(BeneficioService.hashCodigo("token-de-prueba"));
        qr.setBeneficio(beneficio);
        qr.setFechaVencimiento(LocalDateTime.of(2026, 11, 16, 0, 0));
        when(usuarioDAO.buscarPorCorreo("ana@example.com")).thenReturn(usuario);
        when(sesionDAO.buscarPorIdYUsuario(9, 7)).thenReturn(sesion);
        when(qrDAO.buscarPorCodigoConBloqueo(BeneficioService.hashCodigo("token-de-prueba"))).thenReturn(qr);
    }

    @Test
    void consumeUnaVezYAsociaBeneficioASesion() {
        ResultadoValidacionQr resultado = servicio.validar("ana@example.com", 9, "token-de-prueba");

        assertTrue(resultado.aprobada());
        assertEquals(BigDecimal.ONE, sesion.getHorasGratuitas());
        assertTrue(qr.isConsumido());
        verify(sesionDAO).actualizar(sesion);
        verify(qrDAO).actualizar(qr);
        verify(validacionDAO).crear(any(ValidacionQr.class));
    }

    @Test
    void rechazaQrYaConsumidoYRegistraElIntentoSinReaplicarlo() {
        qr.setConsumido(true);

        ResultadoValidacionQr resultado = servicio.validar("ana@example.com", 9, "token-de-prueba");

        assertFalse(resultado.aprobada());
        assertEquals(BigDecimal.ZERO, sesion.getHorasGratuitas());
        verify(qrDAO, never()).actualizar(qr);
        verify(sesionDAO, never()).actualizar(sesion);
        verify(validacionDAO).crear(org.mockito.ArgumentMatchers.argThat(
                validacion -> ValidacionQr.RECHAZADA.equals(validacion.getResultado())));
    }
}
