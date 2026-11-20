package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.MovimientoAhorro;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class MovimientoAhorroDAOImplementado extends DAOJpa<MovimientoAhorro> implements MovimientoAhorroDAO {

    public MovimientoAhorroDAOImplementado() {
        super(MovimientoAhorro.class);
    }

    @Override
    public List<MovimientoAhorro> listarPorUsuario(Integer idUsuario) {
        return em.createQuery(
                        "select m from MovimientoAhorro m "
                                + "join fetch m.cuenta c "
                                + "where c.usuario.id = :idUsuario "
                                + "order by m.fechaHora desc, m.id desc",
                        MovimientoAhorro.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();
    }
}
