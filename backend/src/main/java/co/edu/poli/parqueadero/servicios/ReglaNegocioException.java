package co.edu.poli.parqueadero.servicios;

/** Error de una regla del negocio; su mensaje se puede mostrar al usuario. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
