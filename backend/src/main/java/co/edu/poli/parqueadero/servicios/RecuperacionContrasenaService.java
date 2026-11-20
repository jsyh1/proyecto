package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.RecuperacionContrasenaDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.RecuperacionContrasena;
import co.edu.poli.parqueadero.modelo.Usuario;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RecuperacionContrasenaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecuperacionContrasenaService.class);
    private static final Duration VIGENCIA_TOKEN = Duration.ofMinutes(15);
    private static final int LONGITUD_TOKEN_BYTES = 32;
    private static final int MINIMO_CARACTERES_CONTRASENA = 8;
    private static final int MAXIMO_BYTES_BCRYPT = 72;

    private final UsuarioDAO usuarioDAO;
    private final RecuperacionContrasenaDAO recuperacionDAO;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final Clock reloj;
    private final SecureRandom generadorAleatorio = new SecureRandom();
    private final String correoRemitente;
    private final String urlRestablecimiento;

    public RecuperacionContrasenaService(
            UsuarioDAO usuarioDAO,
            RecuperacionContrasenaDAO recuperacionDAO,
            JavaMailSender mailSender,
            PasswordEncoder passwordEncoder,
            Clock reloj,
            @Value("${app.password-recovery.from}") String correoRemitente,
            @Value("${app.password-recovery.reset-url}") String urlRestablecimiento) {
        this.usuarioDAO = usuarioDAO;
        this.recuperacionDAO = recuperacionDAO;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.reloj = reloj;
        this.correoRemitente = correoRemitente;
        this.urlRestablecimiento = urlRestablecimiento;
    }

    @Transactional
    public void solicitar(String correoIngresado) {
        if (correoIngresado == null || correoIngresado.isBlank()) {
            return;
        }
        String correo = correoIngresado.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario == null) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now(reloj);
        recuperacionDAO.invalidarActivasPorUsuario(usuario.getId(), ahora);
        String token = generarToken();
        RecuperacionContrasena recuperacion = new RecuperacionContrasena();
        recuperacion.setUsuario(usuario);
        recuperacion.setTokenHash(hashToken(token));
        recuperacion.setFechaCreacion(ahora);
        recuperacion.setFechaExpiracion(ahora.plus(VIGENCIA_TOKEN));
        recuperacionDAO.crear(recuperacion);

        try {
            enviarCorreo(usuario.getCorreo(), token);
        } catch (MailException excepcion) {
            recuperacionDAO.invalidarActivasPorUsuario(usuario.getId(), ahora);
            LOGGER.error("No se pudo enviar el enlace de recuperación para el usuario id={}.",
                    usuario.getId(), excepcion);
        }
    }

    @Transactional
    public void restablecer(String token, String nuevaContrasena) {
        validarContrasena(nuevaContrasena);
        RecuperacionContrasena recuperacion = token == null || token.isBlank() || token.length() > 128
                ? null
                : recuperacionDAO.buscarBloqueadaPorHash(hashToken(token));
        LocalDateTime ahora = LocalDateTime.now(reloj);
        if (recuperacion == null
                || recuperacion.getFechaUso() != null
                || recuperacion.getFechaExpiracion() == null
                || !recuperacion.getFechaExpiracion().isAfter(ahora)) {
            throw enlaceInvalido();
        }

        Usuario usuario = recuperacion.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(nuevaContrasena));
        usuarioDAO.actualizar(usuario);
        recuperacionDAO.invalidarActivasPorUsuario(usuario.getId(), ahora);
    }

    private void enviarCorreo(String destinatario, String token) {
        String enlace = UriComponentsBuilder.fromUriString(urlRestablecimiento)
                .queryParam("token", token)
                .build()
                .toUriString();
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(correoRemitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Restablecimiento de contraseña");
        mensaje.setText("Recibimos una solicitud para restablecer tu contraseña.\n\n"
                + "Abre este enlace dentro de los próximos 15 minutos y úsalo una sola vez:\n"
                + enlace + "\n\n"
                + "Si no solicitaste este cambio, puedes ignorar este mensaje.");
        mailSender.send(mensaje);
    }

    private String generarToken() {
        byte[] bytes = new byte[LONGITUD_TOKEN_BYTES];
        generadorAleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException excepcion) {
            throw new IllegalStateException("No está disponible SHA-256 para proteger el token.", excepcion);
        }
    }

    private static void validarContrasena(String contrasena) {
        if (contrasena == null
                || contrasena.length() < MINIMO_CARACTERES_CONTRASENA
                || contrasena.getBytes(StandardCharsets.UTF_8).length > MAXIMO_BYTES_BCRYPT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres y no superar 72 bytes UTF-8.");
        }
    }

    private static ResponseStatusException enlaceInvalido() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace no es válido o ya expiró.");
    }
}
