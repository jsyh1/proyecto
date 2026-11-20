package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.RecuperacionContrasena;
import java.time.LocalDateTime;
import java.util.List;
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
        List<?> ids = em.createNativeQuery(
                        "select id_recuperacion from RECUPERACION_CONTRASENA where token_hash = :tokenHash for update")
                .setParameter("tokenHash", tokenHash)
                .getResultList();
        return ids.isEmpty()
                ? null
                : em.find(RecuperacionContrasena.class, ((Number) ids.get(0)).intValue());
    }
}
