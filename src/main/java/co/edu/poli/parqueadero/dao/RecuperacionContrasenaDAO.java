package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.RecuperacionContrasena;
import java.time.LocalDateTime;

public interface RecuperacionContrasenaDAO extends DAO<RecuperacionContrasena> {

    void invalidarActivasPorUsuario(Integer idUsuario, LocalDateTime fechaUso);

    RecuperacionContrasena buscarBloqueadaPorHash(String tokenHash);
}
