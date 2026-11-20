package co.edu.poli.parqueadero.controlador;

import co.edu.poli.parqueadero.modelo.Comercio;

public record EstablecimientoResponse(Integer id, String nombre, String direccion, String estado) {

    public static EstablecimientoResponse desde(Comercio comercio) {
        return new EstablecimientoResponse(
                comercio.getId(), comercio.getNombre(), comercio.getDireccion(), comercio.getEstado());
    }
}
