package co.edu.poli.parqueadero.controlador;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitudRecuperacionForm(
        @NotBlank @Email @Size(max = 100) String correo) {
}
