package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.servicios.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.SecurityContextRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SecurityContextRepository securityContextRepository;

    public UsuarioController(
            UsuarioService usuarioService,
            SecurityContextRepository securityContextRepository) {
        this.usuarioService = usuarioService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroUsuarioRequest solicitud) {
        return UsuarioResponse.desde(usuarioService.registrar(solicitud));
    }

    @GetMapping("/me")
    public UsuarioResponse perfil(Authentication autenticacion) {
        return UsuarioResponse.desde(usuarioService.buscarPorCorreoAutenticado(autenticacion.getName()));
    }

    @PutMapping("/me")
    public UsuarioResponse actualizar(
            Authentication autenticacion,
            @Valid @RequestBody PerfilUsuarioRequest solicitud,
            HttpServletRequest request,
            HttpServletResponse response) {
        Usuario usuario = usuarioService.actualizarPerfil(autenticacion.getName(), solicitud);
        Authentication nuevaAutenticacion = new UsernamePasswordAuthenticationToken(
                usuario.getCorreo(), null, autenticacion.getAuthorities());
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(nuevaAutenticacion);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);
        return UsuarioResponse.desde(usuario);
    }
}
