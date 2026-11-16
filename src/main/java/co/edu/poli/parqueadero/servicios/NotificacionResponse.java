package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.modelo.Notificacion;
import java.time.LocalDateTime;

public record NotificacionResponse(
        Integer id,
        Integer idSesion,
        String mensaje,
        boolean leida,
        LocalDateTime fechaCreacion) {

    public static NotificacionResponse desde(Notificacion notificacion) {
        return new NotificacionResponse(notificacion.getId(), notificacion.getSesion().getId(),
                notificacion.getMensaje(), notificacion.isLeida(), notificacion.getFechaCreacion());
    }
}
