package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Tarifa;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class HistorialSesionServiceTest {

    private SesionDAO sesionDAO;
    private UsuarioDAO usuarioDAO;
    private HistorialSesionService servicio;

    @BeforeEach
    void setUp() {
        sesionDAO = org.mockito.Mockito.mock(SesionDAO.class);
        usuarioDAO = org.mockito.Mockito.mock(UsuarioDAO.class);
        servicio = new HistorialSesionService(sesionDAO, usuarioDAO);
        when(usuarioDAO.buscarPorId(5)).thenReturn(new Usuario());
    }

    @Test
    void listaSesionesDelUsuarioIncluyendoFechasYValorPagado() {
        SesionParqueadero sesion = crearSesion(12);
        when(sesionDAO.listarPorUsuario(5)).thenReturn(List.of(sesion));

        List<HistorialSesionResponse> historial = servicio.listar(5);

        assertEquals(1, historial.size());
        assertEquals(12, historial.get(0).id());
        assertEquals("ABC123", historial.get(0).placa());
        assertEquals(sesion.getFechaEntrada(), historial.get(0).fechaEntrada());
        assertEquals(sesion.getFechaSalida(), historial.get(0).fechaSalida());
        assertEquals(new BigDecimal("12500.00"), historial.get(0).montoPagado());
    }

    @Test
    void consultaDetalleSoloDeUnaSesionDelUsuario() {
        SesionParqueadero sesion = crearSesion(12);
        when(sesionDAO.buscarPorIdYUsuario(12, 5)).thenReturn(sesion);

        DetalleHistorialSesionResponse detalle = servicio.consultar(5, 12);

        assertEquals("Centro Demo", detalle.centro());
        assertEquals(new BigDecimal("5000.00"), detalle.valorHora());
        assertEquals(new BigDecimal("2.50"), detalle.horasFacturables());
        assertEquals(0, detalle.beneficiosQrAplicados());
        assertEquals(new BigDecimal("12500.00"), detalle.montoPagado());
    }

    @Test
    void noExponeSesionQueNoPerteneceAlUsuario() {
        when(sesionDAO.buscarPorIdYUsuario(12, 5)).thenReturn(null);

        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
                () -> servicio.consultar(5, 12));

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatusCode());
        verify(sesionDAO).buscarPorIdYUsuario(12, 5);
    }

    @Test
    void rechazaUsuarioInexistente() {
        when(usuarioDAO.buscarPorId(99)).thenReturn(null);

        ResponseStatusException excepcion = assertThrows(ResponseStatusException.class,
                () -> servicio.listar(99));

        assertEquals(HttpStatus.NOT_FOUND, excepcion.getStatusCode());
        verifyNoInteractions(sesionDAO);
    }

    private SesionParqueadero crearSesion(Integer id) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("ABC123");
        CentroComercial centro = new CentroComercial();
        centro.setNombre("Centro Demo");
        Tarifa tarifa = new Tarifa();
        tarifa.setValorHora(new BigDecimal("5000.00"));

        SesionParqueadero sesion = new SesionParqueadero();
        sesion.setId(id);
        sesion.setVehiculo(vehiculo);
        sesion.setCentro(centro);
        sesion.setTarifa(tarifa);
        sesion.setFechaEntrada(LocalDateTime.of(2026, 11, 1, 10, 0));
        sesion.setFechaSalida(LocalDateTime.of(2026, 11, 1, 12, 30));
        sesion.setEstado("CERRADA");
        sesion.setHorasFacturables(new BigDecimal("2.50"));
        sesion.setHorasGratuitas(BigDecimal.ZERO);
        sesion.setBeneficiosQrAplicados(0);
        sesion.setMontoTotal(new BigDecimal("12500.00"));
        return sesion;
    }
}
