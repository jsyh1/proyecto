package co.edu.poli.parqueadero.dao;

import java.util.List;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;

public interface SesionDAO extends DAO<SesionParqueadero> {

    /** Sesión abierta de un vehículo; null si no tiene ninguna. */
    SesionParqueadero buscarAbiertaPorVehiculo(Integer idVehiculo);

    /** Sesión abierta asociada a una placa ya normalizada; null si no existe. */
    SesionParqueadero buscarAbiertaPorPlaca(String placa);

    /** Historial de sesiones de un usuario, de la más reciente a la más antigua. */
    List<SesionParqueadero> listarPorUsuario(Integer idUsuario);

    /** Busca una sesión solo si pertenece al usuario indicado; null si no existe. */
    SesionParqueadero buscarPorIdYUsuario(Integer idSesion, Integer idUsuario);
}
