package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.dao.NotificacionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Notificacion;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Usuario;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificacionService {

    private final NotificacionDAO notificacionDAO;
    private final UsuarioDAO usuarioDAO;
    private final Clock reloj;

    public NotificacionService(NotificacionDAO notificacionDAO, UsuarioDAO usuarioDAO, Clock reloj) {
        this.notificacionDAO = notificacionDAO;
        this.usuarioDAO = usuarioDAO;
        this.reloj = reloj;
    }

    @Transactional
    public void entradaRegistrada(SesionParqueadero sesion) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(sesion.getVehiculo().getUsuario());
        notificacion.setSesion(sesion);
        notificacion.setMensaje("El vehículo " + sesion.getVehiculo().getPlaca()
                + " ingresó a " + sesion.getCentro().getNombre()
                + " a las " + sesion.getFechaEntrada() + ".");
        notificacion.setFechaCreacion(LocalDateTime.now(reloj));
        notificacion.setLeida(false);
        notificacionDAO.crear(notificacion);
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listar(String correo) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe.");
        }
        return notificacionDAO.listarPorUsuario(usuario.getId()).stream()
                .map(NotificacionResponse::desde).toList();
    }
}
