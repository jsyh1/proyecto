package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.Tarifa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class TarifaServiceTest {

    @Test
    void impideVigenciasActivasTraslapadasEnElMismoCentro() {
        TarifaDAO tarifaDAO = mock(TarifaDAO.class);
        CentroComercialDAO centroDAO = mock(CentroComercialDAO.class);
        TarifaService servicio = new TarifaService(tarifaDAO, centroDAO);
        CentroComercial centro = new CentroComercial();
        centro.setId(3);
        centro.setEstado(CentroComercial.ESTADO_ACTIVO);
        Tarifa existente = new Tarifa();
        existente.setId(8);
        existente.setFechaInicio(LocalDate.of(2026, 1, 1));
        existente.setFechaFin(LocalDate.of(2026, 12, 31));
        existente.setEstado(Tarifa.ESTADO_ACTIVA);
        when(centroDAO.buscarPorId(3)).thenReturn(centro);
        when(tarifaDAO.listarPorCentro(3)).thenReturn(List.of(existente));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> servicio.crear(new TarifaService.SolicitudTarifa(
                        3, new BigDecimal("7000"), LocalDate.of(2026, 4, 1), null, "ACTIVA")));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(tarifaDAO, org.mockito.Mockito.never()).crear(org.mockito.ArgumentMatchers.any());
    }
}
