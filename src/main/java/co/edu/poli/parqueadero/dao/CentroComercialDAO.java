package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.CentroComercial;
import java.util.List;

public interface CentroComercialDAO extends DAO<CentroComercial> {

    List<CentroComercial> listarActivos();
}
