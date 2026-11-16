package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.poli.parqueadero.dao.BeneficioDAO;
import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.QrBeneficioDAO;
import co.edu.poli.parqueadero.dao.SesionDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.dao.ValidacionQrDAO;
import co.edu.poli.parqueadero.dao.VehiculoDAO;
import co.edu.poli.parqueadero.modelo.Beneficio;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.QrBeneficio;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Tarifa;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ValidacionQrIntegrationTest {

    @Autowired private CentroComercialDAO centroDAO;
    @Autowired private UsuarioDAO usuarioDAO;
    @Autowired private VehiculoDAO vehiculoDAO;
    @Autowired private TarifaDAO tarifaDAO;
    @Autowired private SesionDAO sesionDAO;
    @Autowired private BeneficioDAO beneficioDAO;
    @Autowired private QrBeneficioDAO qrDAO;
    @Autowired private ValidacionQrDAO validacionDAO;
    @Autowired private ValidacionQrService servicio;

    @Test
    void persisteValidacionBloqueaElQrYPermiteConsultarHistorialYTarifa() {
        CentroComercial centro = new CentroComercial();
        centro.setNombre("Centro de prueba");
        centro.setEstado(CentroComercial.ESTADO_ACTIVO);
        centroDAO.crear(centro);

        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellido("Prueba");
        usuario.setDocumento("int-1001");
        usuario.setCorreo("qr-test@example.test");
        usuario.setPasswordHash("hash-de-prueba");
        usuario.setRol("USER");
        usuarioDAO.crear(usuario);

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setUsuario(usuario);
        vehiculo.setPlaca("QRT123");
        vehiculoDAO.crear(vehiculo);

        Tarifa tarifa = new Tarifa();
        tarifa.setCentro(centro);
        tarifa.setValorHora(new BigDecimal("5000.00"));
        tarifa.setFechaInicio(LocalDate.now().minusDays(1));
        tarifa.setEstado(Tarifa.ESTADO_ACTIVA);
        tarifaDAO.crear(tarifa);

        SesionParqueadero sesion = new SesionParqueadero();
        sesion.setVehiculo(vehiculo);
        sesion.setCentro(centro);
        sesion.setTarifa(tarifa);
        sesion.setFechaEntrada(LocalDateTime.now().minusMinutes(30));
        sesion.setHorasGratuitas(BigDecimal.ZERO);
        sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);
        sesionDAO.crear(sesion);

        Beneficio beneficio = new Beneficio();
        beneficio.setCentro(centro);
        beneficio.setNombre("Treinta minutos gratis");
        beneficio.setTipo(Beneficio.HORAS_GRATIS);
        beneficio.setValor(new BigDecimal("0.50"));
        beneficio.setFechaInicio(LocalDate.now().minusDays(1));
        beneficio.setActivo(true);
        beneficioDAO.crear(beneficio);

        String codigo = "token-integracion-qr";
        QrBeneficio qr = new QrBeneficio();
        qr.setCodigoHash(BeneficioService.hashCodigo(codigo));
        qr.setBeneficio(beneficio);
        qr.setFechaVencimiento(LocalDateTime.now().plusDays(1));
        qrDAO.crear(qr);

        ResultadoValidacionQr resultado = servicio.validar(usuario.getCorreo(), sesion.getId(), codigo);

        assertTrue(resultado.aprobada());
        assertTrue(qr.isConsumido());
        assertEquals(new BigDecimal("0.50"), sesion.getHorasGratuitas());
        assertEquals(1, validacionDAO.listarAprobadasPorSesion(sesion.getId()).size());
        assertEquals(1, validacionDAO.listarPorUsuario(usuario.getId()).size());
        assertEquals(1, tarifaDAO.listarPorCentro(centro.getId()).size());
        assertEquals(1, beneficioDAO.listarPorCentro(centro.getId()).size());
    }
}
