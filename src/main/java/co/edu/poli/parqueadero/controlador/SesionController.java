package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.servicios.ReglaNegocioException;
import co.edu.poli.parqueadero.servicios.ResumenSesion;
import co.edu.poli.parqueadero.servicios.SesionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sesiones")
public class SesionController {

    private final SesionService servicio;

    public SesionController(SesionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/entrada")
    public String formularioEntrada(Model modelo) {
        modelo.addAttribute("centros", servicio.listarCentrosActivos());
        return "sesiones/entrada";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(@RequestParam String placa, @RequestParam Integer idCentro, Model modelo) {
        try {
            SesionParqueadero sesion = servicio.registrarEntrada(placa, idCentro);
            return "redirect:/sesiones/" + sesion.getId();
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("error", e.getMessage());
            modelo.addAttribute("placa", placa);
            modelo.addAttribute("centros", servicio.listarCentrosActivos());
            return "sesiones/entrada";
        }
    }

    @PostMapping("/salida")
    public String registrarSalida(@RequestParam String placa, RedirectAttributes atributos) {
        try {
            ResumenSesion resumen = servicio.registrarSalidaPorPlaca(placa);
            atributos.addFlashAttribute("mensajeSalida", "La salida se registró correctamente.");
            return "redirect:/sesiones/" + resumen.id();
        } catch (ReglaNegocioException e) {
            atributos.addFlashAttribute("errorConsulta", e.getMessage());
            return "redirect:/sesiones/entrada";
        }
    }

    @GetMapping("/consulta")
    public String consultarPorPlaca(@RequestParam String placa, RedirectAttributes atributos) {
        try {
            ResumenSesion resumen = servicio.consultarSesionAbiertaPorPlaca(placa);
            return "redirect:/sesiones/" + resumen.id();
        } catch (ReglaNegocioException e) {
            atributos.addFlashAttribute("errorConsulta", e.getMessage());
            return "redirect:/sesiones/entrada";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model modelo, RedirectAttributes atributos) {
        try {
            modelo.addAttribute("resumen", servicio.consultarSesion(id));
            return "sesiones/detalle";
        } catch (ReglaNegocioException e) {
            atributos.addFlashAttribute("errorConsulta", e.getMessage());
            return "redirect:/sesiones/entrada";
        }
    }
}
