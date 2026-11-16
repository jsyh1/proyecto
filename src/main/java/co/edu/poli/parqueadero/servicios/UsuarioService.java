package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.controlador.PerfilUsuarioRequest;
import co.edu.poli.parqueadero.controlador.RegistroUsuarioRequest;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroUsuarioRequest solicitud) {
        if (solicitud.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña excede el límite seguro de 72 bytes.");
        }
        String correo = normalizarCorreo(solicitud.correo());
        String documento = solicitud.documento().trim();
        if (usuarioDAO.buscarPorCorreo(correo) != null || usuarioDAO.buscarPorDocumento(documento) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El correo o documento ya está registrado.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellido(solicitud.apellido().trim());
        usuario.setDocumento(documento);
        usuario.setCorreo(correo);
        usuario.setTelefono(limpiar(solicitud.telefono()));
        usuario.setPasswordHash(passwordEncoder.encode(solicitud.password()));
        usuario.setRol("USER");
        usuarioDAO.crear(usuario);
        return usuario;
    }

    @Transactional
    public Usuario actualizarPerfil(String correoActual, PerfilUsuarioRequest solicitud) {
        Usuario usuario = exigirCorreo(correoActual);
        String correoNuevo = normalizarCorreo(solicitud.correo());
        Usuario existente = usuarioDAO.buscarPorCorreo(correoNuevo);
        if (existente != null && !existente.getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado.");
        }
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellido(solicitud.apellido().trim());
        usuario.setCorreo(correoNuevo);
        usuario.setTelefono(limpiar(solicitud.telefono()));
        usuarioDAO.actualizar(usuario);
        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorCorreoAutenticado(String correo) {
        return exigirCorreo(correo);
    }

    private Usuario exigirCorreo(String correo) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(normalizarCorreo(correo));
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe.");
        }
        return usuario;
    }

    private static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private static String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
