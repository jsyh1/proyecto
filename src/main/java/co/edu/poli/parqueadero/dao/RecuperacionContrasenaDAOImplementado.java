package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.RecuperacionContrasena;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

@Repository
public class RecuperacionContrasenaDAOImplementado
        extends DAOJpa<RecuperacionContrasena>
        implements RecuperacionContrasenaDAO {

    public RecuperacionContrasenaDAOImplementado() {
        super(RecuperacionContrasena.class);
    }

    @Override
    public void invalidarActivasPorUsuario(Integer idUsuario, LocalDateTime fechaUso) {
        em.createQuery(
                        "update RecuperacionContrasena r set r.fechaUso = :fechaUso "
                                + "where r.usuario.id = :idUsuario and r.fechaUso is null")
                .setParameter("fechaUso", fechaUso)
                .setParameter("idUsuario", idUsuario)
                .executeUpdate();
    }

    @Override
    public RecuperacionContrasena buscarBloqueadaPorHash(String tokenHash) {
        return primero(em.createQuery(
                        "select r from RecuperacionContrasena r where r.tokenHash = :tokenHash",
                        RecuperacionContrasena.class)
                .setParameter("tokenHash", tokenHash)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList());
    }
}
