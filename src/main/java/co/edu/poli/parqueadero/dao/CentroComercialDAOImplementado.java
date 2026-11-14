package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.CentroComercial;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CentroComercialDAOImplementado extends DAOJpa<CentroComercial> implements CentroComercialDAO {

    public CentroComercialDAOImplementado() {
        super(CentroComercial.class);
    }

    @Override
    public List<CentroComercial> listarActivos() {
        return em.createQuery("select c from CentroComercial c where c.estado = :estado order by c.nombre",
                CentroComercial.class)
                .setParameter("estado", CentroComercial.ESTADO_ACTIVO)
                .getResultList();
    }
}
