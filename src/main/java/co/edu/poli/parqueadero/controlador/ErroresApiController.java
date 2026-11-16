package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.ReglaNegocioException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErroresApiController {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> reglaNegocio(ReglaNegocioException error) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", error.getMessage()));
    }
}
