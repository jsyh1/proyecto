package co.edu.poli.parqueadero.servicios;

import java.math.BigDecimal;
import java.util.List;

public record ResumenCobro(
        Integer idSesion,
        String placa,
        String centro,
        String estado,
        BigDecimal costoInicial,
        BigDecimal descuentoBeneficios,
        BigDecimal valorFinal,
        List<String> beneficiosAplicados) {
}
