package co.edu.poli.parqueadero.servicios;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionReloj {

    /** Reloj del sistema en hora de Colombia; se inyecta para poder probar el cálculo del tiempo. */
    @Bean
    public Clock reloj() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }
}
