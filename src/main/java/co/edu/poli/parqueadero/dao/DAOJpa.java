package co.edu.poli.parqueadero.dao;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Implementación base del CRUD genérico sobre JPA. Cada DAO concreto
 * hereda estas operaciones y solo agrega sus consultas propias.
 */
public abstract class DAOJpa<T> implements DAO<T> {

    @PersistenceContext
    protected EntityManager em;

    private final Class<T> tipo;

    protected DAOJpa(Class<T> tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean crear(T objeto) {
        em.persist(objeto);
        return true;
    }

    @Override
    public List<T> listar() {
        return em.createQuery("select e from " + tipo.getSimpleName() + " e", tipo).getResultList();
    }

    @Override
    public T buscarPorId(Integer id) {
        return em.find(tipo, id);
    }

    @Override
    public boolean actualizar(T objeto) {
        em.merge(objeto);
        return true;
    }

    @Override
    public boolean eliminar(Integer id) {
        T entidad = em.find(tipo, id);
        if (entidad == null) {
            return false;
        }
        em.remove(entidad);
        return true;
    }

    protected <R> R primero(List<R> lista) {
        return lista.isEmpty() ? null : lista.get(0);
    }
}
