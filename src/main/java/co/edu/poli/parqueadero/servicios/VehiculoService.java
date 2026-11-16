package co.edu.poli.parqueadero.servicios;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.dao.VehiculoDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import java.util.List;

@Service
public class VehiculoService {

    private final VehiculoDAO vehiculoDAO;
    private final UsuarioDAO usuarioDAO;

    public VehiculoService(VehiculoDAO vehiculoDAO, UsuarioDAO usuarioDAO) {
        this.vehiculoDAO = vehiculoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    @Transactional
    public Vehiculo registrar(Integer idUsuario, String placaIngresada, String marca, String modelo, String color) {
        String placa = ReglasPlaca.normalizar(placaIngresada);
        if (!ReglasPlaca.esValida(placa)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La placa ingresada no es válida.");
        }

        if (vehiculoDAO.buscarPorPlaca(placa) != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La placa " + placa + " ya está registrada.");
        }

        Usuario usuario = idUsuario == null ? null : usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario seleccionado no existe.");
        }

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setUsuario(usuario);
        vehiculo.setPlaca(placa);
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setColor(color);
        vehiculoDAO.crear(vehiculo);
        return vehiculo;
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listarPorUsuario(Integer idUsuario) {
        return vehiculoDAO.listarPorUsuario(idUsuario);
    }
}
