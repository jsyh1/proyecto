package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.QrBeneficio;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

@Repository
public class QrBeneficioDAOImplementado extends DAOJpa<QrBeneficio> implements QrBeneficioDAO {

    public QrBeneficioDAOImplementado() {
        super(QrBeneficio.class);
    }

    @Override
    public QrBeneficio buscarPorCodigoConBloqueo(String codigoHash) {
        return primero(em.createQuery(
                "select q from QrBeneficio q join fetch q.beneficio b join fetch b.centro "
                        + "where q.codigoHash = :codigo",
                QrBeneficio.class)
                .setParameter("codigo", codigoHash)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .setMaxResults(1)
                .getResultList());
    }
}
