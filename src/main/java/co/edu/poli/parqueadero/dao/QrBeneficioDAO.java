package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.QrBeneficio;

public interface QrBeneficioDAO extends DAO<QrBeneficio> {

    QrBeneficio buscarPorCodigoConBloqueo(String codigo);
}
