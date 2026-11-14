package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.dao.VehiculoDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class VehiculoServiceTest {

    private VehiculoDAO vehiculoDAO;
    private UsuarioDAO usuarioDAO;
    private VehiculoService servicio;

    @BeforeEach
    void setUp() {
        vehiculoDAO = org.mockito.Mockito.mock(VehiculoDAO.class);
        usuarioDAO = org.mockito.Mockito.mock(UsuarioDAO.class);
        servicio = new VehiculoService(vehiculoDAO, usuarioDAO);
    }

    @Test
    void registraPlacaNormalizadaYLaAsociaAlUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(8);
        when(usuarioDAO.buscarPorId(8)).thenReturn(usuario);

        Vehiculo registrado = servicio.registrar(8, " abc-123 ", "Mazda", "3", "Rojo");

        assertEquals("ABC123", registrado.getPlaca());
        assertEquals(usuario, registrado.getUsuario());
        assertEquals("Mazda", registrado.getMarca());
        verify(vehiculoDAO).crear(registrado);
    }

    @Test
    void rechazaPlacaConFormatoInvalido() {
        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
                () -> servicio.registrar(8, "AB-1", null, null, null));

        assertEquals(HttpStatus.BAD_REQUEST, excepcion.getStatusCode());
        verify(vehiculoDAO, never()).crear(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaPlacaDuplicadaSinImportarElUsuario() {
        when(vehiculoDAO.buscarPorPlaca("ABC123")).thenReturn(new Vehiculo());

        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
                () -> servicio.registrar(8, "abc123", null, null, null));

        assertEquals(HttpStatus.CONFLICT, excepcion.getStatusCode());
        verify(usuarioDAO, never()).buscarPorId(8);
        verify(vehiculoDAO, never()).crear(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rechazaUsuarioInexistente() {
        when(usuarioDAO.buscarPorId(8)).thenReturn(null);

        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
                () -> servicio.registrar(8, "ABC123", null, null, null));

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatusCode());
        assertTrue(excepcion.getReason().contains("usuario"));
        verify(vehiculoDAO, never()).crear(org.mockito.ArgumentMatchers.any());
    }
}
