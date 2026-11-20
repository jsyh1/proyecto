package co.edu.poli.parqueadero.controlador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstablecimientoRequest(
        @NotBlank @Size(max = 80) String nombre,
        @Size(max = 120) String direccion) {
}
