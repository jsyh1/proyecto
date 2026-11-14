package co.edu.poli.parqueadero.controlador;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = VehiculoController.class)
public class VehiculoExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail manejarConflictoDeIntegridad() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "El vehículo entra en conflicto con un registro existente.");
    }
}
