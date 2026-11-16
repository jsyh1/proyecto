package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Notificacion;
import java.util.List;

public interface NotificacionDAO extends DAO<Notificacion> {

    List<Notificacion> listarPorUsuario(Integer idUsuario);
}
