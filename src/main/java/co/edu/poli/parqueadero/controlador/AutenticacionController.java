package co.edu.poli.parqueadero.controlador;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private static final Logger LOG = LoggerFactory.getLogger(AutenticacionController.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AutenticacionController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/csrf")
    public Map<String, String> token(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }

    @PostMapping("/login")
    public Map<String, String> iniciarSesion(
            @Valid @RequestBody CredencialesRequest solicitud,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            Authentication autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            solicitud.correo().trim().toLowerCase(), solicitud.password()));
            request.getSession(true);
            request.changeSessionId();
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(autenticacion);
            SecurityContextHolder.setContext(contexto);
            securityContextRepository.saveContext(contexto, request, response);
            LOG.info("Inicio de sesión exitoso");
            return Map.of("mensaje", "Sesión iniciada correctamente.");
        } catch (BadCredentialsException e) {
            LOG.warn("Inicio de sesión rechazado por credenciales inválidas");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }
    }

    @PostMapping("/logout")
    public Map<String, String> cerrarSesion(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication autenticacion) {
        new SecurityContextLogoutHandler().logout(request, response, autenticacion);
        return Map.of("mensaje", "Sesión cerrada correctamente.");
    }

    public record CredencialesRequest(
            @jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String correo,
            @jakarta.validation.constraints.NotBlank String password) {
    }
}
