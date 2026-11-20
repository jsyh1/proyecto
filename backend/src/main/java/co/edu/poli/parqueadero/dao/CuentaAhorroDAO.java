package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.CuentaAhorro;

public interface CuentaAhorroDAO extends DAO<CuentaAhorro> {

    CuentaAhorro buscarPorUsuario(Integer idUsuario);

    CuentaAhorro buscarOCrearBloqueada(Integer idUsuario);
}
