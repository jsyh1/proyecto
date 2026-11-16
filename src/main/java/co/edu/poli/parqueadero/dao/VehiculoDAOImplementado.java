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
    public List<Vehiculo> listarPorUsuario(Integer idUsuario) {
        return em.createQuery(
                "select v from Vehiculo v where v.usuario.id = :idUsuario order by v.placa",
                Vehiculo.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();
    }

    @Override
    public Vehiculo buscarPorPlaca(String placa) {
        return primero(em.createQuery("select v from Vehiculo v where v.placa = :placa", Vehiculo.class)
                .setParameter("placa", placa)
                .getResultList());
    }
}
