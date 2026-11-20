package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.BeneficioQrAplicado;

public interface BeneficioQrDAO extends DAO<BeneficioQrAplicado> {

    BeneficioQrAplicado buscarPorClaveCompraHash(String claveCompraHash);
}
