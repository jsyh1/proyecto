package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.servicios.CentroComercialAdminService;
import co.edu.poli.parqueadero.servicios.ReglaNegocioException;
import java.math.BigDecimal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/administracion/centros-comerciales")
public class CentroComercialAdminWebController {

    private final CentroComercialAdminService servicio;

    public CentroComercialAdminWebController(CentroComercialAdminService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public String listar(Model modelo) {
        return cargarVista(modelo, null);
    }

    @PostMapping
    public String registrar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) BigDecimal valorHora,
            Model modelo,
            RedirectAttributes atributos) {
        try {
            servicio.registrar(nombre, direccion, valorHora);
            atributos.addFlashAttribute("mensaje", "Centro comercial creado con tarifa activa.");
            return "redirect:/administracion/centros-comerciales";
        } catch (ReglaNegocioException excepcion) {
            modelo.addAttribute("nombre", nombre);
            modelo.addAttribute("direccion", direccion);
            modelo.addAttribute("valorHora", valorHora);
            return cargarVista(modelo, excepcion.getMessage());
        }
    }

    private String cargarVista(Model modelo, String error) {
        modelo.addAttribute("centros", servicio.listar());
        if (error != null) {
            modelo.addAttribute("error", error);
        }
        return "admin/centros-comerciales";
    }
}
