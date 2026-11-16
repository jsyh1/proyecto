package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Pago;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PagoDAOImplementado extends DAOJpa<Pago> implements PagoDAO {

    public PagoDAOImplementado() {
        super(Pago.class);
    }

    @Override
    public Pago buscarPagoAprobado(Integer idSesion) {
        List<Pago> pagos = em.createQuery(
                "select p from Pago p where p.sesion.id = :idSesion and p.estado = 'APROBADO'",
                Pago.class)
                .setParameter("idSesion", idSesion)
                .setMaxResults(1)
                .getResultList();
        return primero(pagos);
    }
}
