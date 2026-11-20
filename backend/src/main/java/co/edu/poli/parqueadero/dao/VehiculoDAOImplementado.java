package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Vehiculo;
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
        List<?> ids = em.createNativeQuery(
                        "select id_vehiculo from VEHICULO where placa = :placa for update")
                .setParameter("placa", placa)
                .getResultList();
        if (ids.isEmpty()) {
            return null;
        }
        return em.find(Vehiculo.class, ((Number) ids.get(0)).intValue());
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
