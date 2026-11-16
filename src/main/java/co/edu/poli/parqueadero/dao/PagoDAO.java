package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Pago;

public interface PagoDAO extends DAO<Pago> {

    Pago buscarPagoAprobado(Integer idSesion);
}
