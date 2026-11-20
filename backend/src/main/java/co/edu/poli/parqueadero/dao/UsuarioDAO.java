package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Usuario;

public interface UsuarioDAO extends DAO<Usuario> {

    Usuario buscarPorCorreo(String correo);
}
