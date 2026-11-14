package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Vehiculo;

public interface VehiculoDAO extends DAO<Vehiculo> {

    /** Busca un vehículo por su placa ya normalizada; null si no existe. */
    Vehiculo buscarPorPlaca(String placa);
}
