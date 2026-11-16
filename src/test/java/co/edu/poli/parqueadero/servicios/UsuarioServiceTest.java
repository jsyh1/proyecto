package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.controlador.RegistroUsuarioRequest;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class UsuarioServiceTest {

    private UsuarioDAO usuarioDAO;
    private UsuarioService servicio;

    @BeforeEach
    void setUp() {
        usuarioDAO = org.mockito.Mockito.mock(UsuarioDAO.class);
        servicio = new UsuarioService(usuarioDAO, new BCryptPasswordEncoder(4));
    }

    @Test
    void registraCorreoNormalizadoYGuardaHashNoReversible() {
        when(usuarioDAO.buscarPorCorreo("ana@example.com")).thenReturn(null);
        when(usuarioDAO.buscarPorDocumento("123")).thenReturn(null);

        Usuario usuario = servicio.registrar(new RegistroUsuarioRequest(
                "Ana", "Pérez", "123", " ANA@EXAMPLE.COM ", "3001234567", "mi-clave-segura"));

        assertEquals("ana@example.com", usuario.getCorreo());
        assertNotEquals("mi-clave-segura", usuario.getPasswordHash());
        assertEquals("USER", usuario.getRol());
        verify(usuarioDAO).crear(usuario);
    }

    @Test
    void noRegistraCorreoODocumentoDuplicado() {
        when(usuarioDAO.buscarPorCorreo("ana@example.com")).thenReturn(new Usuario());

        assertThrows(ResponseStatusException.class, () -> servicio.registrar(
                new RegistroUsuarioRequest("Ana", "Pérez", "123", "ana@example.com", null, "mi-clave-segura")));

        verify(usuarioDAO, never()).crear(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaContrasenasQueExcedenElLimiteDeBytesDeBcrypt() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.registrar(new RegistroUsuarioRequest(
                        "Ana", "Pérez", "123", "ana@example.com", null, "á".repeat(40))));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(usuarioDAO, never()).crear(org.mockito.ArgumentMatchers.any());
    }
}
