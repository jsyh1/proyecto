package co.edu.poli.parqueadero.dao;

import java.util.List;

import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import org.springframework.stereotype.Repository;

@Repository
public class SesionDAOImplementado extends DAOJpa<SesionParqueadero> implements SesionDAO {

    public SesionDAOImplementado() {
        super(SesionParqueadero.class);
    }

    @Override
    public SesionParqueadero buscarAbiertaPorVehiculo(Integer idVehiculo) {
        return primero(em.createQuery(
                "select s from SesionParqueadero s where s.vehiculo.id = :idVehiculo and s.estado = :estado",
                SesionParqueadero.class)
                .setParameter("idVehiculo", idVehiculo)
                .setParameter("estado", SesionParqueadero.ESTADO_ABIERTA)
                .getResultList());
    }

    @Override
    public SesionParqueadero buscarAbiertaPorPlaca(String placa) {
        return primero(em.createQuery(
                "select s from SesionParqueadero s where s.vehiculo.placa = :placa and s.estado = :estado",
                SesionParqueadero.class)
                .setParameter("placa", placa)
                .setParameter("estado", SesionParqueadero.ESTADO_ABIERTA)
                .getResultList());
    }

    @Override
    public SesionParqueadero buscarAbiertaPorPlacaBloqueada(String placa) {
        List<?> ids = em.createNativeQuery(
                "select id_sesion from SESION_PARQUEADERO "
                        + "where id_vehiculo = (select id_vehiculo from VEHICULO where placa = :placa) "
                        + "and estado = :estado for update")
                .setParameter("placa", placa)
                .setParameter("estado", SesionParqueadero.ESTADO_ABIERTA)
                .getResultList();
        if (ids.isEmpty()) {
            return null;
        }
        return em.find(SesionParqueadero.class, ((Number) ids.get(0)).intValue());
    }

    @Override
    public SesionParqueadero buscarPorIdBloqueado(Integer idSesion) {
        if (idSesion == null) {
            return null;
        }
        List<?> ids = em.createNativeQuery(
                        "select id_sesion from SESION_PARQUEADERO where id_sesion = :idSesion for update")
                .setParameter("idSesion", idSesion)
                .getResultList();
        return ids.isEmpty() ? null : em.find(SesionParqueadero.class, ((Number) ids.get(0)).intValue());
    }

    @Override
    public List<SesionParqueadero> listarPorUsuario(Integer idUsuario) {
        return em.createQuery(
                "select s from SesionParqueadero s "
                        + "join fetch s.vehiculo v "
                        + "join fetch s.centro "
                        + "join fetch s.tarifa "
                        + "where v.usuario.id = :idUsuario "
                        + "order by s.fechaEntrada desc, s.id desc",
                SesionParqueadero.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();
    }

    @Override
    public SesionParqueadero buscarPorIdYUsuario(Integer idSesion, Integer idUsuario) {
        return primero(em.createQuery(
                "select s from SesionParqueadero s "
                        + "join fetch s.vehiculo v "
                        + "join fetch s.centro "
                        + "join fetch s.tarifa "
                        + "where s.id = :idSesion and v.usuario.id = :idUsuario",
                SesionParqueadero.class)
                .setParameter("idSesion", idSesion)
                .setParameter("idUsuario", idUsuario)
                .getResultList());
    }
}
