package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.RegistroAcceso;
import org.springframework.stereotype.Repository;

@Repository
public class RegistroAccesoDAOImplementado extends DAOJpa<RegistroAcceso> implements RegistroAccesoDAO {

    public RegistroAccesoDAOImplementado() {
        super(RegistroAcceso.class);
    }
}
