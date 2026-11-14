package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Usuario;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAOImplementado extends DAOJpa<Usuario> implements UsuarioDAO {

    public UsuarioDAOImplementado() {
        super(Usuario.class);
    }
}
