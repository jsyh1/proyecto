package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.BeneficioQrAplicado;
import org.springframework.stereotype.Repository;

@Repository
public class BeneficioQrDAOImplementado extends DAOJpa<BeneficioQrAplicado> implements BeneficioQrDAO {

    public BeneficioQrDAOImplementado() {
        super(BeneficioQrAplicado.class);
    }

    @Override
    public BeneficioQrAplicado buscarPorClaveCompraHash(String claveCompraHash) {
        return primero(em.createQuery(
                        "select b from BeneficioQrAplicado b join fetch b.sesion "
                                + "where b.claveCompraHash = :claveCompraHash",
                        BeneficioQrAplicado.class)
                .setParameter("claveCompraHash", claveCompraHash)
                .getResultList());
    }
}
