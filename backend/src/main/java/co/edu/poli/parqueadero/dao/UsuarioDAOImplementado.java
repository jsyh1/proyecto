package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Usuario;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAOImplementado extends DAOJpa<Usuario> implements UsuarioDAO {

    public UsuarioDAOImplementado() {
        super(Usuario.class);
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        return primero(em.createQuery(
                        "select u from Usuario u where lower(u.correo) = :correo",
                        Usuario.class)
                .setParameter("correo", correo)
                .getResultList());
    }
}
