package co.edu.poli.parqueadero.controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import co.edu.poli.parqueadero.servicios.RecuperacionContrasenaService;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class RecuperacionContrasenaControllerTest {

    @Test
    void muestraLaMismaRespuestaParaCualquierCorreoValido() {
        RecuperacionContrasenaController controlador =
                new RecuperacionContrasenaController(mock(RecuperacionContrasenaService.class));

        String respuestaCuentaExistente = solicitar(controlador, "usuario@example.com");
        String respuestaCuentaDesconocida = solicitar(controlador, "otro@example.com");

        assertEquals(respuestaCuentaExistente, respuestaCuentaDesconocida);
        assertEquals("Si el correo corresponde a una cuenta, recibirás instrucciones para recuperar el acceso.",
                respuestaCuentaExistente);
    }

    private String solicitar(RecuperacionContrasenaController controlador, String correo) {
        SolicitudRecuperacionForm formulario = new SolicitudRecuperacionForm(correo);
        RedirectAttributesModelMap atributos = new RedirectAttributesModelMap();
        String vista = controlador.solicitar(
                formulario,
                new BeanPropertyBindingResult(formulario, "solicitud"),
                new ExtendedModelMap(),
                atributos);

        assertEquals("redirect:/recuperar-contrasena", vista);
        return (String) atributos.getFlashAttributes().get("mensaje");
    }
}
