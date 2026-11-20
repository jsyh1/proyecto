package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.CuentaAhorro;
import co.edu.poli.parqueadero.modelo.Usuario;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

@Repository
public class CuentaAhorroDAOImplementado extends DAOJpa<CuentaAhorro> implements CuentaAhorroDAO {

    public CuentaAhorroDAOImplementado() {
        super(CuentaAhorro.class);
    }

    @Override
    public CuentaAhorro buscarPorUsuario(Integer idUsuario) {
        return primero(em.createQuery(
                        "select c from CuentaAhorro c where c.usuario.id = :idUsuario",
                        CuentaAhorro.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList());
    }

    @Override
    public CuentaAhorro buscarOCrearBloqueada(Integer idUsuario) {
        Usuario usuario = em.find(Usuario.class, idUsuario, LockModeType.PESSIMISTIC_WRITE);
        if (usuario == null) {
            return null;
        }

        CuentaAhorro cuenta = primero(em.createQuery(
                        "select c from CuentaAhorro c where c.usuario.id = :idUsuario",
                        CuentaAhorro.class)
                .setParameter("idUsuario", idUsuario)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList());
        if (cuenta == null) {
            cuenta = new CuentaAhorro();
            cuenta.setUsuario(usuario);
            cuenta.setSaldoHoras(BigDecimal.ZERO);
            em.persist(cuenta);
        }
        return cuenta;
    }
}
