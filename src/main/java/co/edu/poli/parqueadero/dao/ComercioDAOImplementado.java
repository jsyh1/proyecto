package co.edu.poli.parqueadero.dao;

import org.springframework.stereotype.Repository;

import co.edu.poli.parqueadero.modelo.Comercio;

@Repository
public class ComercioDAOImplementado extends DAOJpa<Comercio> implements ComercioDAO {

    public ComercioDAOImplementado() {
        super(Comercio.class);
    }
}
