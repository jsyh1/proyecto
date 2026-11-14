package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.modelo.Vehiculo;

public record VehiculoResponse(
        Integer id,
        Integer idUsuario,
        String placa,
        String marca,
        String modelo,
        String color) {

    static VehiculoResponse desde(Vehiculo vehiculo) {
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getUsuario().getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getColor());
    }
}
