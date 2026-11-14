package co.edu.poli.parqueadero.servicios;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;

@Service
public class HistorialSesionService {

    private final SesionDAO sesionDAO;
    private final UsuarioDAO usuarioDAO;

    public HistorialSesionService(SesionDAO sesionDAO, UsuarioDAO usuarioDAO) {
        this.sesionDAO = sesionDAO;
        this.usuarioDAO = usuarioDAO;
    }

    @Transactional(readOnly = true)
    public List<HistorialSesionResponse> listar(Integer idUsuario) {
        validarUsuario(idUsuario);
        return sesionDAO.listarPorUsuario(idUsuario).stream()
                .map(HistorialSesionResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public DetalleHistorialSesionResponse consultar(Integer idUsuario, Integer idSesion) {
        validarUsuario(idUsuario);
        var sesion = sesionDAO.buscarPorIdYUsuario(idSesion, idUsuario);
        if (sesion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "La sesión no existe para el usuario indicado.");
        }
        return DetalleHistorialSesionResponse.desde(sesion);
    }

    private void validarUsuario(Integer idUsuario) {
        if (idUsuario == null || usuarioDAO.buscarPorId(idUsuario) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario solicitado no existe.");
        }
    }
}
