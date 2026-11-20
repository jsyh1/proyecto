package co.edu.poli.parqueadero.controlador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerContrasenaForm(
        String token,
        @NotBlank @Size(min = 8, max = 72) String nuevaContrasena,
        @NotBlank String confirmacion) {
}
