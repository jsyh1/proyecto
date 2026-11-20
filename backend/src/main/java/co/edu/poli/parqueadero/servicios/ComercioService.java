package co.edu.poli.parqueadero.servicios;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.poli.parqueadero.dao.ComercioDAO;
import co.edu.poli.parqueadero.modelo.Comercio;

@Service
public class ComercioService {

    private final ComercioDAO comercioDAO;

    public ComercioService(ComercioDAO comercioDAO) {
        this.comercioDAO = comercioDAO;
    }

    @Transactional
    public Comercio registrarEstablecimiento(String nombre, String direccion) {
        Comercio comercio = new Comercio();
        comercio.setNombre(normalizarNombre(nombre));
        comercio.setDireccion(normalizarDireccion(direccion));
        comercio.setEstado(Comercio.ESTADO_ACTIVO);
        if (!comercioDAO.crear(comercio)) {
            throw new ReglaNegocioException("No se pudo registrar el establecimiento.");
        }
        return comercio;
    }

    @Transactional(readOnly = true)
    public List<Comercio> consultarEstablecimientos() {
        return comercioDAO.listar();
    }

    @Transactional(readOnly = true)
    public Comercio consultarEstablecimiento(Integer id) {
        return buscarPorId(id);
    }

    @Transactional
    public Comercio modificarEstablecimiento(Integer id, String nombre, String direccion) {
        Comercio comercio = buscarPorId(id);
        comercio.setNombre(normalizarNombre(nombre));
        comercio.setDireccion(normalizarDireccion(direccion));
        return comercio;
    }

    @Transactional
    public Comercio activarEstablecimiento(Integer id) {
        Comercio comercio = buscarPorId(id);
        comercio.setEstado(Comercio.ESTADO_ACTIVO);
        return comercio;
    }

    @Transactional
    public Comercio desactivarEstablecimiento(Integer id) {
        Comercio comercio = buscarPorId(id);
        comercio.setEstado(Comercio.ESTADO_INACTIVO);
        return comercio;
    }

    @Transactional(readOnly = true)
    public Comercio obtenerEstablecimientoActivoParaValidacion(Integer id) {
        Comercio comercio = buscarPorId(id);
        if (!Comercio.ESTADO_ACTIVO.equals(comercio.getEstado())) {
            throw new ReglaNegocioException("El establecimiento no está activo para recibir validaciones.");
        }
        return comercio;
    }

    private Comercio buscarPorId(Integer id) {
        Comercio comercio = id == null ? null : comercioDAO.buscarPorId(id);
        if (comercio == null) {
            throw new ReglaNegocioException("El establecimiento solicitado no existe.");
        }
        return comercio;
    }

    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del establecimiento es obligatorio.");
        }
        String normalizado = nombre.trim();
        if (normalizado.length() > 80) {
            throw new ReglaNegocioException("El nombre del establecimiento no puede superar 80 caracteres.");
        }
        return normalizado;
    }

    private String normalizarDireccion(String direccion) {
        if (direccion == null || direccion.isBlank()) {
            return null;
        }
        String normalizada = direccion.trim();
        if (normalizada.length() > 120) {
            throw new ReglaNegocioException("La dirección no puede superar 120 caracteres.");
        }
        return normalizada;
    }
}
