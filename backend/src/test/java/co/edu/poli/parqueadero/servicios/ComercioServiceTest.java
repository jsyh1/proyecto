package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.poli.parqueadero.dao.ComercioDAO;
import co.edu.poli.parqueadero.modelo.Comercio;

class ComercioServiceTest {

    @Test
    void registraEstablecimientoActivoConDatosNormalizados() {
        ComercioDAO comercioDAO = mock(ComercioDAO.class);
        when(comercioDAO.crear(any(Comercio.class))).thenReturn(true);

        Comercio comercio = new ComercioService(comercioDAO)
                .registrarEstablecimiento("  Tienda Central  ", "  Calle 1  ");

        assertEquals("Tienda Central", comercio.getNombre());
        assertEquals("Calle 1", comercio.getDireccion());
        assertEquals(Comercio.ESTADO_ACTIVO, comercio.getEstado());
    }

    @Test
    void permiteConsultarYModificarUnEstablecimientoSinCambiarSuEstado() {
        ComercioDAO comercioDAO = mock(ComercioDAO.class);
        Comercio comercio = crearComercio(Comercio.ESTADO_INACTIVO);
        when(comercioDAO.buscarPorId(1)).thenReturn(comercio);

        Comercio consultado = new ComercioService(comercioDAO).consultarEstablecimiento(1);
        Comercio actualizado = new ComercioService(comercioDAO)
                .modificarEstablecimiento(1, "Tienda Nueva", "Carrera 2");

        assertEquals(comercio, consultado);
        assertEquals("Tienda Nueva", actualizado.getNombre());
        assertEquals("Carrera 2", actualizado.getDireccion());
        assertEquals(Comercio.ESTADO_INACTIVO, actualizado.getEstado());
    }

    @Test
    void permiteListarEstablecimientosActivosEInactivos() {
        ComercioDAO comercioDAO = mock(ComercioDAO.class);
        List<Comercio> establecimientos = List.of(
                crearComercio(Comercio.ESTADO_ACTIVO),
                crearComercio(Comercio.ESTADO_INACTIVO));
        when(comercioDAO.listar()).thenReturn(establecimientos);

        assertEquals(establecimientos, new ComercioService(comercioDAO).consultarEstablecimientos());
    }

    @Test
    void activacionYDesactivacionCambianElEstado() {
        ComercioDAO comercioDAO = mock(ComercioDAO.class);
        Comercio comercio = crearComercio(Comercio.ESTADO_ACTIVO);
        when(comercioDAO.buscarPorId(1)).thenReturn(comercio);
        ComercioService servicio = new ComercioService(comercioDAO);

        assertEquals(Comercio.ESTADO_INACTIVO, servicio.desactivarEstablecimiento(1).getEstado());
        assertEquals(Comercio.ESTADO_ACTIVO, servicio.activarEstablecimiento(1).getEstado());
    }

    @Test
    void soloLosEstablecimientosActivosPuedenUsarseEnValidaciones() {
        ComercioDAO comercioDAO = mock(ComercioDAO.class);
        Comercio inactivo = crearComercio(Comercio.ESTADO_INACTIVO);
        when(comercioDAO.buscarPorId(1)).thenReturn(inactivo);

        ComercioService servicio = new ComercioService(comercioDAO);
        assertThrows(ReglaNegocioException.class, () -> servicio.obtenerEstablecimientoActivoParaValidacion(1));
        assertEquals(Comercio.ESTADO_INACTIVO, inactivo.getEstado());

        inactivo.setEstado(Comercio.ESTADO_ACTIVO);
        assertEquals(inactivo, servicio.obtenerEstablecimientoActivoParaValidacion(1));
    }

    @Test
    void validaDatosObligatoriosYExistenciaDelEstablecimiento() {
        ComercioDAO comercioDAO = mock(ComercioDAO.class);
        ComercioService servicio = new ComercioService(comercioDAO);

        assertThrows(ReglaNegocioException.class, () -> servicio.registrarEstablecimiento(" ", null));
        assertThrows(ReglaNegocioException.class, () -> servicio.consultarEstablecimiento(null));
    }

    private Comercio crearComercio(String estado) {
        Comercio comercio = new Comercio();
        comercio.setId(1);
        comercio.setNombre("Tienda Central");
        comercio.setDireccion("Calle 1");
        comercio.setEstado(estado);
        return comercio;
    }
}
