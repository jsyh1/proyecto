package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioDetailsService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario == null) {
            throw new UsernameNotFoundException("Credenciales inválidas.");
        }
        return User.withUsername(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol())
                .build();
    }
}
