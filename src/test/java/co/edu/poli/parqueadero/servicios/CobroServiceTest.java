package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.dao.ValidacionQrDAO;
import co.edu.poli.parqueadero.modelo.Beneficio;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.QrBeneficio;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.ValidacionQr;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CobroServiceTest {

    @Test
    void calculaElDescuentoQrSobreElCostoPosteriorALasHorasGratis() {
        SesionDAO sesionDAO = mock(SesionDAO.class);
        UsuarioDAO usuarioDAO = mock(UsuarioDAO.class);
        ValidacionQrDAO validacionDAO = mock(ValidacionQrDAO.class);
        SesionService sesionService = mock(SesionService.class);
        CobroService servicio = new CobroService(sesionDAO, usuarioDAO, validacionDAO, sesionService);

        SesionParqueadero sesion = new SesionParqueadero();
        sesion.setId(5);
        sesion.setVehiculo(new co.edu.poli.parqueadero.modelo.Vehiculo());
        sesion.getVehiculo().setPlaca("ABC123");
        CentroComercial centro = new CentroComercial();
        centro.setNombre("Centro Demo");
        sesion.setCentro(centro);
        sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);
        when(sesionService.consultarSesion(5)).thenReturn(new ResumenSesion(
                5, "ABC123", "Centro Demo", "15/11/2026 10:00", "ABIERTA",
                60, "1 h", new BigDecimal("100.00"), new BigDecimal("0.25"), new BigDecimal("75.00")));

        Beneficio beneficio = new Beneficio();
        beneficio.setNombre("10% adicional");
        beneficio.setTipo(Beneficio.PORCENTAJE_DESCUENTO);
        beneficio.setValor(new BigDecimal("10"));
        QrBeneficio qr = new QrBeneficio();
        qr.setBeneficio(beneficio);
        ValidacionQr validacion = new ValidacionQr();
        validacion.setQr(qr);
        when(validacionDAO.listarAprobadasPorSesion(5)).thenReturn(List.of(validacion));

        ResumenCobro resultado = servicio.calcular(sesion);

        assertEquals(new BigDecimal("100.00"), resultado.costoInicial());
        assertEquals(new BigDecimal("32.50"), resultado.descuentoBeneficios());
        assertEquals(new BigDecimal("67.50"), resultado.valorFinal());
    }
}
