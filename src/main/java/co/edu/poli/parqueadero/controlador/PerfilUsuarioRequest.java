package co.edu.poli.parqueadero.controlador;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilUsuarioRequest(
        @NotBlank @Size(max = 50) String nombre,
        @NotBlank @Size(max = 50) String apellido,
        @NotBlank @Email @Size(max = 100) String correo,
        @Pattern(regexp = "^$|^[0-9+() -]{7,20}$") String telefono) {
}
