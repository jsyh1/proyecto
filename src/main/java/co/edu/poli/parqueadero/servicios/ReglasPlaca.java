package co.edu.poli.parqueadero.servicios;

import java.util.Locale;
import java.util.regex.Pattern;

final class ReglasPlaca {

    private static final Pattern FORMATO = Pattern.compile("^[A-Z0-9]{5,7}$");

    private ReglasPlaca() {
    }

    static String normalizar(String placa) {
        return placa == null ? "" : placa.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    static boolean esValida(String placa) {
        return FORMATO.matcher(placa).matches();
    }
}
