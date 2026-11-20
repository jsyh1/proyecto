package co.edu.poli.parqueadero.servicios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
        @NotBlank @Size(max = 50) String nombre,
        @NotBlank @Size(max = 50) String apellido,
        @NotBlank @Size(max = 20) String documento,
        @NotBlank @Email @Size(max = 100) String correo,
        @Size(max = 20) String telefono,
        @NotBlank @Size(min = 8, max = 72) String contrasena) {
}
