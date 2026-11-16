package co.edu.poli.parqueadero.controlador;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioRequest(
        @NotBlank @Size(max = 50) String nombre,
        @NotBlank @Size(max = 50) String apellido,
        @NotBlank @Size(max = 20) String documento,
        @NotBlank @Email @Size(max = 100) String correo,
        @Pattern(regexp = "^$|^[0-9+() -]{7,20}$") String telefono,
        @NotBlank @Size(min = 10, max = 72) String password) {
}
