package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Tarifa;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class TarifaDAOImplementado extends DAOJpa<Tarifa> implements TarifaDAO {

    public TarifaDAOImplementado() {
        super(Tarifa.class);
    }

    @Override
    public Tarifa buscarVigente(Integer idCentro, LocalDate fecha) {
        return primero(em.createQuery(
                "select t from Tarifa t where t.centro.id = :idCentro and t.estado = :estado "
                        + "and t.fechaInicio <= :fecha and (t.fechaFin is null or t.fechaFin >= :fecha) "
                        + "order by t.fechaInicio desc", Tarifa.class)
                .setParameter("idCentro", idCentro)
                .setParameter("estado", Tarifa.ESTADO_ACTIVA)
                .setParameter("fecha", fecha)
                .setMaxResults(1)
                .getResultList());
    }

    @Override
    public List<Tarifa> listarPorCentro(Integer idCentro) {
        return em.createQuery(
                "select t from Tarifa t join fetch t.centro c where c.id = :idCentro "
                        + "order by t.fechaInicio desc, t.id desc",
                Tarifa.class)
                .setParameter("idCentro", idCentro)
                .getResultList();
    }
}
