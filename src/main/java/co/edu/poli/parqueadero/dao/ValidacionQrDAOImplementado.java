package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.ValidacionQr;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ValidacionQrDAOImplementado extends DAOJpa<ValidacionQr> implements ValidacionQrDAO {

    public ValidacionQrDAOImplementado() {
        super(ValidacionQr.class);
    }

    @Override
    public List<ValidacionQr> listarPorUsuario(Integer idUsuario) {
        return em.createQuery(
                "select v from ValidacionQr v "
                        + "join fetch v.sesion s join fetch s.centro "
                        + "left join fetch v.qr q left join fetch q.beneficio "
                        + "where v.usuario.id = :idUsuario order by v.fechaValidacion desc, v.id desc",
                ValidacionQr.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();
    }

    @Override
    public List<ValidacionQr> listarAprobadasPorSesion(Integer idSesion) {
        return em.createQuery(
                "select v from ValidacionQr v join fetch v.qr q join fetch q.beneficio "
                        + "where v.sesion.id = :idSesion and v.resultado = :resultado "
                        + "order by v.fechaValidacion",
                ValidacionQr.class)
                .setParameter("idSesion", idSesion)
                .setParameter("resultado", ValidacionQr.APROBADA)
                .getResultList();
    }
}
