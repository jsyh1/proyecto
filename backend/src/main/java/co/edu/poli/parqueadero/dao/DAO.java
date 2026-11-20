package co.edu.poli.parqueadero.dao;

import java.util.List;

public interface DAO<T> {

    boolean crear(T objeto);

    List<T> listar();

    T buscarPorId(Integer id);

    boolean actualizar(T objeto);

    boolean eliminar(Integer id);
}
