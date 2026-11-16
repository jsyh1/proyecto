package co.edu.poli.parqueadero.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginasUsuarioController {

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/usuarios/registro")
    public String registro() {
        return "usuarios/registro";
    }

    @GetMapping("/usuarios/login")
    public String login() {
        return "usuarios/login";
    }

    @GetMapping("/usuarios/perfil")
    public String perfil() {
        return "usuarios/perfil";
    }

    @GetMapping("/usuarios/validaciones")
    public String validaciones() {
        return "usuarios/validaciones";
    }

    @GetMapping("/usuarios/notificaciones")
    public String notificaciones() {
        return "usuarios/notificaciones";
    }

    @GetMapping("/admin/tarifas")
    public String tarifas() {
        return "admin/tarifas";
    }

    @GetMapping("/admin/beneficios")
    public String beneficios() {
        return "admin/beneficios";
    }
}
