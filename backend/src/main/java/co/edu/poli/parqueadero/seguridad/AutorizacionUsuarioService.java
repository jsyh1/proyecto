package co.edu.poli.parqueadero.seguridad;

import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AutorizacionUsuarioService {

    private final UsuarioDAO usuarioDAO;

    public AutorizacionUsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public void validarAcceso(Integer idUsuario, Authentication autenticacion) {
        boolean esAdmin = autenticacion.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (esAdmin) {
            return;
        }

        Usuario usuarioAutenticado = usuarioDAO.buscarPorCorreo(autenticacion.getName());
        if (usuarioAutenticado == null || !usuarioAutenticado.getId().equals(idUsuario)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes permiso para consultar o modificar los datos de ese usuario.");
        }
    }
}
