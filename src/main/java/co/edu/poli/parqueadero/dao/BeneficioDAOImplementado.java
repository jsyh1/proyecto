package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Beneficio;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class BeneficioDAOImplementado extends DAOJpa<Beneficio> implements BeneficioDAO {

    public BeneficioDAOImplementado() {
        super(Beneficio.class);
    }

    @Override
    public List<Beneficio> listarPorCentro(Integer idCentro) {
        return em.createQuery(
                "select b from Beneficio b join fetch b.centro c where c.id = :idCentro "
                        + "order by b.fechaInicio desc, b.id desc",
                Beneficio.class)
                .setParameter("idCentro", idCentro)
                .getResultList();
    }
}
