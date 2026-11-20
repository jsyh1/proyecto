package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import co.edu.poli.parqueadero.servicios.VehiculoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mis-vehiculos")
public class VehiculoWebController {

    private final VehiculoService vehiculoService;
    private final UsuarioDAO usuarioDAO;

    public VehiculoWebController(VehiculoService vehiculoService, UsuarioDAO usuarioDAO) {
        this.vehiculoService = vehiculoService;
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping
    public String formulario() {
        return "vehiculos/registro";
    }

    @PostMapping
    public String registrar(
            @RequestParam String placa,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String modelo,
            @RequestParam(required = false) String color,
            Authentication authentication,
            Model model,
            RedirectAttributes attributes) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(authentication.getName());
        try {
            Vehiculo vehiculo = vehiculoService.registrar(usuario.getId(), placa, marca, modelo, color);
            attributes.addFlashAttribute("mensaje", "Vehículo " + vehiculo.getPlaca() + " registrado. Ya puedes crear su ingreso.");
            return "redirect:/sesiones/entrada";
        } catch (ResponseStatusException exception) {
            model.addAttribute("error", exception.getReason());
            model.addAttribute("placa", placa);
            model.addAttribute("marca", marca);
            model.addAttribute("modelo", modelo);
            model.addAttribute("color", color);
            return "vehiculos/registro";
        }
    }
}
