package co.edu.poli.parqueadero.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.SesionParqueadero;
import co.edu.poli.parqueadero.modelo.Tarifa;
import co.edu.poli.parqueadero.modelo.Usuario;
import co.edu.poli.parqueadero.modelo.Vehiculo;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:software133;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SesionServiceConcurrenciaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private SesionService sesionService;

    @Test
    void serializaEntradasYEvitaAcreditarDosVecesLaMismaCompraQr() throws Exception {
        DatosSesion datosEntrada = crearDatos("ABC123", "entrada", false);
        DatosSesion datosQr = crearDatos("XYZ987", "qr", true);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            CountDownLatch entradasListas = new CountDownLatch(2);
            CountDownLatch iniciarEntradas = new CountDownLatch(1);
            Future<Boolean> primeraEntrada = executor.submit(entradaConcurrente(
                    datosEntrada, entradasListas, iniciarEntradas));
            Future<Boolean> segundaEntrada = executor.submit(entradaConcurrente(
                    datosEntrada, entradasListas, iniciarEntradas));

            assertTrue(entradasListas.await(10, TimeUnit.SECONDS));
            iniciarEntradas.countDown();
            assertEquals(1, (primeraEntrada.get(10, TimeUnit.SECONDS) ? 1 : 0)
                    + (segundaEntrada.get(10, TimeUnit.SECONDS) ? 1 : 0));
            assertEquals(1L, contarSesionesAbiertas(datosEntrada.idVehiculo()));

            CountDownLatch beneficiosListos = new CountDownLatch(2);
            CountDownLatch iniciarBeneficios = new CountDownLatch(1);
            Future<ResumenSesion> primerBeneficio = executor.submit(beneficioConcurrente(
                    datosQr.idSesion(), beneficiosListos, iniciarBeneficios));
            Future<ResumenSesion> segundoBeneficio = executor.submit(beneficioConcurrente(
                    datosQr.idSesion(), beneficiosListos, iniciarBeneficios));

            assertTrue(beneficiosListos.await(10, TimeUnit.SECONDS));
            iniciarBeneficios.countDown();
            assertEquals(1, primerBeneficio.get(10, TimeUnit.SECONDS).beneficiosQrAplicados());
            assertEquals(1, segundoBeneficio.get(10, TimeUnit.SECONDS).beneficiosQrAplicados());
            assertEquals(new BigDecimal("1.000000"), consultarSesion(datosQr.idSesion()).getHorasGratuitas());
            assertEquals(1L, contarBeneficiosQr(datosQr.idSesion()));
            assertEquals(2L, contarMovimientos(datosQr.idUsuario()));
        } finally {
            executor.shutdownNow();
        }
    }

    private Callable<Boolean> entradaConcurrente(
            DatosSesion datos, CountDownLatch listos, CountDownLatch iniciar) {
        return () -> {
            listos.countDown();
            iniciar.await();
            try {
                sesionService.registrarEntrada("ABC123", datos.idCentro());
                return true;
            } catch (ReglaNegocioException e) {
                return false;
            }
        };
    }

    private Callable<ResumenSesion> beneficioConcurrente(
            Integer idSesion, CountDownLatch listos, CountDownLatch iniciar) {
        return () -> {
            listos.countDown();
            iniciar.await();
            return sesionService.aplicarBeneficioCompraValida(idSesion, "compra-qr-concurrente");
        };
    }

    private DatosSesion crearDatos(String placa, String sufijo, boolean crearSesion) {
        return transaccion().execute(status -> {
            Usuario usuario = new Usuario();
            usuario.setNombre("Prueba");
            usuario.setApellido("Concurrencia");
            usuario.setDocumento("DOC-" + sufijo);
            usuario.setCorreo(sufijo + "@example.test");
            usuario.setPasswordHash("hash");
            entityManager.persist(usuario);

            Vehiculo vehiculo = new Vehiculo();
            vehiculo.setUsuario(usuario);
            vehiculo.setPlaca(placa);
            entityManager.persist(vehiculo);

            CentroComercial centro = new CentroComercial();
            centro.setNombre("Centro " + sufijo);
            centro.setEstado(CentroComercial.ESTADO_ACTIVO);
            entityManager.persist(centro);

            Tarifa tarifa = new Tarifa();
            tarifa.setCentro(centro);
            tarifa.setValorHora(new BigDecimal("5000.00"));
            tarifa.setFechaInicio(LocalDate.of(2020, 1, 1));
            tarifa.setEstado(Tarifa.ESTADO_ACTIVA);
            entityManager.persist(tarifa);

            SesionParqueadero sesion = null;
            if (crearSesion) {
                sesion = new SesionParqueadero();
                sesion.setVehiculo(vehiculo);
                sesion.setCentro(centro);
                sesion.setTarifa(tarifa);
                sesion.setFechaEntrada(LocalDateTime.now());
                sesion.setHorasGratuitas(BigDecimal.ZERO);
                sesion.setEstado(SesionParqueadero.ESTADO_ABIERTA);
                entityManager.persist(sesion);
            }
            entityManager.flush();
            return new DatosSesion(
                    vehiculo.getId(),
                    centro.getId(),
                    usuario.getId(),
                    sesion == null ? null : sesion.getId());
        });
    }

    private long contarSesionesAbiertas(Integer idVehiculo) {
        return transaccion().execute(status -> entityManager.createQuery(
                        "select count(s) from SesionParqueadero s "
                                + "where s.vehiculo.id = :idVehiculo and s.estado = :estado",
                        Long.class)
                .setParameter("idVehiculo", idVehiculo)
                .setParameter("estado", SesionParqueadero.ESTADO_ABIERTA)
                .getSingleResult());
    }

    private long contarBeneficiosQr(Integer idSesion) {
        return transaccion().execute(status -> entityManager.createQuery(
                        "select count(b) from BeneficioQrAplicado b where b.sesion.id = :idSesion",
                        Long.class)
                .setParameter("idSesion", idSesion)
                .getSingleResult());
    }

    private long contarMovimientos(Integer idUsuario) {
        return transaccion().execute(status -> entityManager.createQuery(
                        "select count(m) from MovimientoAhorro m where m.cuenta.usuario.id = :idUsuario",
                        Long.class)
                .setParameter("idUsuario", idUsuario)
                .getSingleResult());
    }

    private SesionParqueadero consultarSesion(Integer idSesion) {
        return transaccion().execute(status -> {
            SesionParqueadero sesion = entityManager.find(SesionParqueadero.class, idSesion);
            assertNotNull(sesion);
            return sesion;
        });
    }

    private TransactionTemplate transaccion() {
        return new TransactionTemplate(transactionManager);
    }

    private record DatosSesion(Integer idVehiculo, Integer idCentro, Integer idUsuario, Integer idSesion) {
    }
}
