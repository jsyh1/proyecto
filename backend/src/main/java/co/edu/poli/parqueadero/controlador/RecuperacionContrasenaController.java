package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.RecuperacionContrasenaService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class RecuperacionContrasenaController {

    private static final String MENSAJE_SOLICITUD =
            "Si el correo corresponde a una cuenta, recibirás instrucciones para recuperar el acceso.";
    private static final String MENSAJE_ENLACE_INVALIDO = "El enlace no es válido o ya expiró.";

    private final RecuperacionContrasenaService servicio;

    public RecuperacionContrasenaController(RecuperacionContrasenaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/recuperar-contrasena")
    public String formularioSolicitud(Model modelo) {
        modelo.addAttribute("solicitud", new SolicitudRecuperacionForm(""));
        return "usuarios/recuperar-contrasena";
    }

    @PostMapping("/recuperar-contrasena")
    public String solicitar(
            @Valid @ModelAttribute("solicitud") SolicitudRecuperacionForm solicitud,
            BindingResult errores,
            Model modelo,
            RedirectAttributes atributos) {
        if (errores.hasErrors()) {
            modelo.addAttribute("error", "Ingresa un correo válido.");
            return "usuarios/recuperar-contrasena";
        }
        servicio.solicitar(solicitud.correo());
        atributos.addFlashAttribute("mensaje", MENSAJE_SOLICITUD);
        return "redirect:/recuperar-contrasena";
    }

    @GetMapping("/restablecer-contrasena")
    public String formularioRestablecimiento(
            @RequestParam(required = false) String token,
            Model modelo) {
        modelo.addAttribute("restablecer", new RestablecerContrasenaForm(token, "", ""));
        return "usuarios/restablecer-contrasena";
    }

    @PostMapping("/restablecer-contrasena")
    public String restablecer(
            @Valid @ModelAttribute("restablecer") RestablecerContrasenaForm formulario,
            BindingResult errores,
            Model modelo) {
        if (errores.hasErrors()) {
            modelo.addAttribute("error", "La contraseña debe tener al menos 8 caracteres.");
            return "usuarios/restablecer-contrasena";
        }
        if (!formulario.nuevaContrasena().equals(formulario.confirmacion())) {
            modelo.addAttribute("error", "Las contraseñas no coinciden.");
            return "usuarios/restablecer-contrasena";
        }
        if (formulario.nuevaContrasena().getBytes(StandardCharsets.UTF_8).length > 72) {
            modelo.addAttribute("error", "La contraseña no puede superar 72 bytes UTF-8.");
            return "usuarios/restablecer-contrasena";
        }
        try {
            servicio.restablecer(formulario.token(), formulario.nuevaContrasena());
            modelo.addAttribute("exito", "La contraseña se cambió correctamente.");
        } catch (ResponseStatusException excepcion) {
            if (excepcion.getStatusCode() != HttpStatus.BAD_REQUEST) {
                throw excepcion;
            }
            modelo.addAttribute("error", MENSAJE_ENLACE_INVALIDO);
        }
        return "usuarios/restablecer-contrasena";
    }
}
