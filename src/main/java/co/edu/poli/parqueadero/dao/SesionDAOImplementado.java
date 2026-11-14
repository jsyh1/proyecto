package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import org.springframework.stereotype.Repository;

@Repository
public class SesionDAOImplementado extends DAOJpa<SesionParqueadero> implements SesionDAO {

    public SesionDAOImplementado() {
        super(SesionParqueadero.class);
    }

    @Override
    public SesionParqueadero buscarAbiertaPorVehiculo(Integer idVehiculo) {
        return primero(em.createQuery(
                "select s from SesionParqueadero s where s.vehiculo.id = :idVehiculo and s.estado = :estado",
                SesionParqueadero.class)
                .setParameter("idVehiculo", idVehiculo)
                .setParameter("estado", SesionParqueadero.ESTADO_ABIERTA)
                .getResultList());
    }

    @Override
    public SesionParqueadero buscarAbiertaPorPlaca(String placa) {
        return primero(em.createQuery(
                "select s from SesionParqueadero s where s.vehiculo.placa = :placa and s.estado = :estado",
                SesionParqueadero.class)
                .setParameter("placa", placa)
                .setParameter("estado", SesionParqueadero.ESTADO_ABIERTA)
                .getResultList());
    }
}
