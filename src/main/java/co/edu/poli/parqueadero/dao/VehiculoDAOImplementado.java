package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Vehiculo;
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
}
