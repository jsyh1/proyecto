package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.ValidacionQr;
import java.util.List;

public interface ValidacionQrDAO extends DAO<ValidacionQr> {

    List<ValidacionQr> listarPorUsuario(Integer idUsuario);

    List<ValidacionQr> listarAprobadasPorSesion(Integer idSesion);
}
