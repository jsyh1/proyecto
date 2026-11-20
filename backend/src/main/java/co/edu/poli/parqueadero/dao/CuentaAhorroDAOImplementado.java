package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.CuentaAhorro;
import co.edu.poli.parqueadero.modelo.Usuario;
import java.math.BigDecimal;
import java.util.List;
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
        List<?> idsUsuario = em.createNativeQuery(
                        "select id_usuario from USUARIO where id_usuario = :idUsuario for update")
                .setParameter("idUsuario", idUsuario)
                .getResultList();
        Usuario usuario = idsUsuario.isEmpty()
                ? null
                : em.find(Usuario.class, ((Number) idsUsuario.get(0)).intValue());
        if (usuario == null) {
            return null;
        }

        List<?> idsCuenta = em.createNativeQuery(
                        "select id_cuenta from CUENTA_AHORRO where id_usuario = :idUsuario for update")
                .setParameter("idUsuario", idUsuario)
                .getResultList();
        CuentaAhorro cuenta = idsCuenta.isEmpty()
                ? null
                : em.find(CuentaAhorro.class, ((Number) idsCuenta.get(0)).intValue());
        if (cuenta == null) {
            cuenta = new CuentaAhorro();
            cuenta.setUsuario(usuario);
            cuenta.setSaldoHoras(BigDecimal.ZERO);
            em.persist(cuenta);
        }
        return cuenta;
    }
}
