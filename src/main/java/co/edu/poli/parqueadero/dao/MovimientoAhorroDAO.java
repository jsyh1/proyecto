package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.MovimientoAhorro;
import java.util.List;

public interface MovimientoAhorroDAO extends DAO<MovimientoAhorro> {

    List<MovimientoAhorro> listarPorUsuario(Integer idUsuario);
}
