package co.edu.poli.parqueadero.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class VehiculoExceptionHandlerTest {

    @Test
    void convierteConflictosDeIntegridadEnRespuestaConflict() {
        ProblemDetail respuesta = new VehiculoExceptionHandler().manejarConflictoDeIntegridad();

        assertEquals(HttpStatus.CONFLICT.value(), respuesta.getStatus());
        assertEquals("El vehículo entra en conflicto con un registro existente.", respuesta.getDetail());
    }
}
