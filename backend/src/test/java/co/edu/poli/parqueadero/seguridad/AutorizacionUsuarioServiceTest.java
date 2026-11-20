package co.edu.poli.parqueadero.seguridad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

class AutorizacionUsuarioServiceTest {

    private UsuarioDAO usuarioDAO;
    private AutorizacionUsuarioService servicio;

    @BeforeEach
    void setUp() {
        usuarioDAO = mock(UsuarioDAO.class);
        servicio = new AutorizacionUsuarioService(usuarioDAO);
    }

    @Test
    void permiteAlUsuarioConsultarSusPropiosDatos() {
        Usuario usuario = new Usuario();
        usuario.setId(7);
        when(usuarioDAO.buscarPorCorreo("usuario@prueba.com")).thenReturn(usuario);

        servicio.validarAcceso(7, autenticacion("usuario@prueba.com", "ROLE_USER"));
    }

    @Test
    void rechazaAccesoDeUnUsuarioALosDatosDeOtraCuenta() {
        Usuario usuario = new Usuario();
        usuario.setId(7);
        when(usuarioDAO.buscarPorCorreo("usuario@prueba.com")).thenReturn(usuario);

        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
                () -> servicio.validarAcceso(8, autenticacion("usuario@prueba.com", "ROLE_USER")));

        assertEquals(HttpStatus.FORBIDDEN, excepcion.getStatusCode());
    }

    @Test
    void permiteAlAdministradorConsultarOtrasCuentas() {
        servicio.validarAcceso(8, autenticacion("admin@prueba.com", "ROLE_ADMIN"));

        verifyNoInteractions(usuarioDAO);
    }

    private static UsernamePasswordAuthenticationToken autenticacion(String correo, String rol) {
        return new UsernamePasswordAuthenticationToken(
                correo, "password", List.of(new SimpleGrantedAuthority(rol)));
    }
}
