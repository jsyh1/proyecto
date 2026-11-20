package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Vehiculo;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class VehiculoDAOImplementado extends DAOJpa<Vehiculo> implements VehiculoDAO {

    public VehiculoDAOImplementado() {
        super(Vehiculo.class);
    }

    @Override
    public Vehiculo buscarPorPlaca(String placa) {
        return primero(em.createQuery("select v from Vehiculo v where v.placa = :placa", Vehiculo.class)
                .setParameter("placa", placa)
                .getResultList());
    }

    @Override
    public Vehiculo buscarPorPlacaBloqueado(String placa) {
        return primero(em.createQuery("select v from Vehiculo v where v.placa = :placa", Vehiculo.class)
                .setParameter("placa", placa)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList());
    }

    @Override
    public List<Vehiculo> buscarPorUsuario(Integer idUsuario) {
        return em.createQuery(
                        "select v from Vehiculo v join fetch v.usuario u "
                                + "where u.id = :idUsuario order by v.placa",
                        Vehiculo.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();
    }
}
