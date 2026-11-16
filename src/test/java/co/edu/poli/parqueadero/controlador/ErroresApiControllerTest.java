package co.edu.poli.parqueadero.controlador;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.poli.parqueadero.servicios.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErroresApiControllerTest {

    @Test
    void reglaDeNegocioEnConsultaRestDevuelve400ConMensaje() {
        var respuesta = new ErroresApiController().reglaNegocio(
                new ReglaNegocioException("No existe una sesión abierta para ese vehículo."));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).containsEntry("mensaje", "No existe una sesión abierta para ese vehículo.");
    }
}
