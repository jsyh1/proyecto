package co.edu.poli.parqueadero.dao;

import co.edu.poli.parqueadero.modelo.Tarifa;
import java.time.LocalDate;

public interface TarifaDAO extends DAO<Tarifa> {

    /** Tarifa activa de un centro comercial para la fecha dada; null si no hay ninguna vigente. */
    Tarifa buscarVigente(Integer idCentro, LocalDate fecha);
}
