package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.dao.RecuperacionContrasenaDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.RecuperacionContrasena;
import co.edu.poli.parqueadero.modelo.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class RecuperacionContrasenaServiceTest {

    private static final String CORREO = "usuario@example.com";
    private static final String URL_RESTABLECIMIENTO = "https://parqueadero.example/restablecer-contrasena";
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private UsuarioDAO usuarioDAO;
    private RecuperacionContrasenaDAO recuperacionDAO;
    private JavaMailSender mailSender;
    private RecuperacionContrasenaService servicio;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuarioDAO = mock(UsuarioDAO.class);
        recuperacionDAO = mock(RecuperacionContrasenaDAO.class);
        mailSender = mock(JavaMailSender.class);
        servicio = new RecuperacionContrasenaService(
                usuarioDAO,
                recuperacionDAO,
                mailSender,
                new BCryptPasswordEncoder(),
                Clock.fixed(Instant.parse("2026-11-19T15:00:00Z"), ZONA),
                "no-reply@example.com",
                URL_RESTABLECIMIENTO);
        usuario = new Usuario();
        usuario.setId(8);
        usuario.setCorreo(CORREO);
    }

    @Test
    void enviaEnlaceDeUnSoloUsoYAlmacenaSoloSuHash() throws Exception {
        when(usuarioDAO.buscarPorCorreo(CORREO)).thenReturn(usuario);
        ArgumentCaptor<RecuperacionContrasena> recuperacionCaptor =
                ArgumentCaptor.forClass(RecuperacionContrasena.class);
        when(recuperacionDAO.crear(recuperacionCaptor.capture())).thenReturn(true);
        ArgumentCaptor<SimpleMailMessage> correoCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        servicio.solicitar("  USUARIO@EXAMPLE.COM ");

        RecuperacionContrasena recuperacion = recuperacionCaptor.getValue();
        SimpleMailMessage mensaje = capturarCorreo(correoCaptor);
        String token = mensaje.getText().substring(mensaje.getText().indexOf("?token=") + 7);
        token = token.substring(0, token.indexOf('\n'));

        assertEquals(hash(token), recuperacion.getTokenHash());
        assertNotEquals(token, recuperacion.getTokenHash());
        assertEquals(LocalDateTime.of(2026, 11, 19, 10, 0), recuperacion.getFechaCreacion());
        assertEquals(LocalDateTime.of(2026, 11, 19, 10, 15), recuperacion.getFechaExpiracion());
        assertEquals(CORREO, mensaje.getTo()[0]);
        assertTrue(mensaje.getText().contains(URL_RESTABLECIMIENTO));
        verify(recuperacionDAO).invalidarActivasPorUsuario(
                8, LocalDateTime.of(2026, 11, 19, 10, 0));
    }

    @Test
    void noEnviaMensajesNiIndicaSiElCorreoNoPerteneceAUnaCuenta() {
        when(usuarioDAO.buscarPorCorreo(CORREO)).thenReturn(null);

        servicio.solicitar(CORREO);

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(recuperacionDAO, never()).crear(any(RecuperacionContrasena.class));
    }

    @Test
    void invalidaElTokenSiFallaElEnvioDeCorreo() {
        when(usuarioDAO.buscarPorCorreo(CORREO)).thenReturn(usuario);
        when(recuperacionDAO.crear(any(RecuperacionContrasena.class))).thenReturn(true);
        org.mockito.Mockito.doThrow(new MailSendException("SMTP no disponible"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        servicio.solicitar(CORREO);

        verify(recuperacionDAO, times(2)).invalidarActivasPorUsuario(
                8, LocalDateTime.of(2026, 11, 19, 10, 0));
    }

    @Test
    void cambiaLaContrasenaYInvalidaLosEnlacesExistentes() throws Exception {
        String token = "token-valido-de-prueba";
        RecuperacionContrasena recuperacion = recuperacionVigente(token);
        when(recuperacionDAO.buscarBloqueadaPorHash(hash(token))).thenReturn(recuperacion);

        servicio.restablecer(token, "NuevaClave123");

        assertNotEquals("NuevaClave123", usuario.getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("NuevaClave123", usuario.getPasswordHash()));
        verify(usuarioDAO).actualizar(usuario);
        verify(recuperacionDAO).invalidarActivasPorUsuario(
                8, LocalDateTime.of(2026, 11, 19, 10, 0));
    }

    @Test
    void rechazaEnlacesExpiradosYUsadosConElMismoMensaje() throws Exception {
        String token = "token-expirado";
        RecuperacionContrasena expiracion = recuperacionVigente(token);
        expiracion.setFechaExpiracion(LocalDateTime.of(2026, 11, 19, 9, 59));
        when(recuperacionDAO.buscarBloqueadaPorHash(hash(token))).thenReturn(expiracion);

        ResponseStatusException vencido = assertThrows(ResponseStatusException.class,
                () -> servicio.restablecer(token, "NuevaClave123"));
        assertEquals(HttpStatus.BAD_REQUEST, vencido.getStatusCode());
        assertEquals("El enlace no es válido o ya expiró.", vencido.getReason());

        expiracion.setFechaExpiracion(LocalDateTime.of(2026, 11, 19, 10, 15));
        expiracion.setFechaUso(LocalDateTime.of(2026, 11, 19, 9, 58));
        ResponseStatusException usado = assertThrows(ResponseStatusException.class,
                () -> servicio.restablecer(token, "NuevaClave123"));
        assertEquals(vencido.getReason(), usado.getReason());
        verify(usuarioDAO, never()).actualizar(any(Usuario.class));
    }

    private RecuperacionContrasena recuperacionVigente(String token) throws Exception {
        RecuperacionContrasena recuperacion = new RecuperacionContrasena();
        recuperacion.setUsuario(usuario);
        recuperacion.setTokenHash(hash(token));
        recuperacion.setFechaCreacion(LocalDateTime.of(2026, 11, 19, 9, 55));
        recuperacion.setFechaExpiracion(LocalDateTime.of(2026, 11, 19, 10, 15));
        when(recuperacionDAO.buscarBloqueadaPorHash(hash(token))).thenReturn(recuperacion);
        return recuperacion;
    }

    private SimpleMailMessage capturarCorreo(ArgumentCaptor<SimpleMailMessage> captor) {
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    private static String hash(String valor) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(valor.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
