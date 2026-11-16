package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Beneficio;
import java.util.List;

public interface BeneficioDAO extends DAO<Beneficio> {

    List<Beneficio> listarPorCentro(Integer idCentro);
}
