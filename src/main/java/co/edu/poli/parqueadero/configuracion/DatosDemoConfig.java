package co.edu.poli.parqueadero.configuracion;

import co.edu.poli.parqueadero.dao.CentroComercialDAO;
import co.edu.poli.parqueadero.dao.TarifaDAO;
import co.edu.poli.parqueadero.modelo.CentroComercial;
import co.edu.poli.parqueadero.modelo.Tarifa;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@Profile("demo")
public class DatosDemoConfig {

    @Bean
    CommandLineRunner inicializarDatosDemo(
            CentroComercialDAO centroDAO,
            TarifaDAO tarifaDAO,
            PlatformTransactionManager transactionManager) {
        return argumentos -> {
            new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
                if (!centroDAO.listarActivos().isEmpty()) {
                    return;
                }
                CentroComercial centro = new CentroComercial();
                centro.setNombre("Centro Comercial Demo");
                centro.setDireccion("Ubicación de demostración");
                centro.setEstado(CentroComercial.ESTADO_ACTIVO);
                centroDAO.crear(centro);

                Tarifa tarifa = new Tarifa();
                tarifa.setCentro(centro);
                tarifa.setValorHora(new BigDecimal("5000.00"));
                tarifa.setFechaInicio(LocalDate.of(2020, 1, 1));
                tarifa.setEstado(Tarifa.ESTADO_ACTIVA);
                tarifaDAO.crear(tarifa);
            });
        };
    }
}
