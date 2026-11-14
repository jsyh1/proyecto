package co.edu.poli.parqueadero.controlador;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RegistrarVehiculoRequest(
        @NotNull @Positive Integer idUsuario,
        @NotBlank @Size(max = 10) String placa,
        @Size(max = 30) String marca,
        @Size(max = 30) String modelo,
        @Size(max = 20) String color) {
}
