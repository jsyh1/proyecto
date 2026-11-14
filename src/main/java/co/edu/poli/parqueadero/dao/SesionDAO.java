package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;

public interface SesionDAO extends DAO<SesionParqueadero> {

    /** Sesión abierta de un vehículo; null si no tiene ninguna. */
    SesionParqueadero buscarAbiertaPorVehiculo(Integer idVehiculo);

    /** Sesión abierta asociada a una placa ya normalizada; null si no existe. */
    SesionParqueadero buscarAbiertaPorPlaca(String placa);
}
