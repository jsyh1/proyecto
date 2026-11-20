package co.edu.poli.parqueadero.servicios;

import co.edu.poli.parqueadero.modelo.Usuario;

public record UsuarioResponse(
        Integer id,
        String nombre,
        String apellido,
        String documento,
        String correo,
        String telefono,
        String rol) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getDocumento(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol());
    }
}
