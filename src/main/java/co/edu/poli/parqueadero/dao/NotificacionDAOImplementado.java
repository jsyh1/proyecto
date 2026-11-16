package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Notificacion;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class NotificacionDAOImplementado extends DAOJpa<Notificacion> implements NotificacionDAO {

    public NotificacionDAOImplementado() {
        super(Notificacion.class);
    }

    @Override
    public List<Notificacion> listarPorUsuario(Integer idUsuario) {
        return em.createQuery(
                "select n from Notificacion n join fetch n.sesion s "
                        + "where n.usuario.id = :idUsuario order by n.fechaCreacion desc, n.id desc",
                Notificacion.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();
    }
}
