package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroUsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final PasswordEncoder passwordEncoder;

    public RegistroUsuarioService(UsuarioDAO usuarioDAO, PasswordEncoder passwordEncoder) {
        this.usuarioDAO = usuarioDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario registrar(RegistroUsuarioRequest solicitud) {
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setApellido(solicitud.apellido().trim());
        usuario.setDocumento(solicitud.documento().trim());
        usuario.setCorreo(solicitud.correo().trim().toLowerCase());
        usuario.setTelefono(solicitud.telefono() == null ? null : solicitud.telefono().trim());
        usuario.setPasswordHash(passwordEncoder.encode(solicitud.contrasena()));
        usuario.setRol("USER");
        if (!usuarioDAO.crear(usuario)) {
            throw new ReglaNegocioException("No se pudo registrar el usuario.");
        }
        return usuario;
    }
}
