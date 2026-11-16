package co.edu.poli.parqueadero.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
public class SeguridadConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityContextRepository contexto,
            CsrfTokenRepository csrf) throws Exception {
        http
                .securityContext(seguridad -> seguridad.securityContextRepository(contexto))
                .csrf(configuracion -> configuracion.csrfTokenRepository(csrf))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf", "/api/centros", "/api/tarifas/vigentes")
                        .permitAll()
                        .requestMatchers("/api/auth/login", "/api/usuarios/registro").permitAll()
                        .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sesiones/entrada").permitAll()
                        .requestMatchers(HttpMethod.POST, "/sesiones/entrada").authenticated()
                        .requestMatchers("/api/sesiones/entradas", "/api/sesiones/consulta").authenticated()
                        .requestMatchers("/api/auth/logout", "/api/usuarios/me/**", "/api/usuarios/me",
                                "/api/validaciones/**", "/api/sesiones/*/beneficios/**",
                                "/api/sesiones/*/pago", "/api/sesiones/*/pagos/**",
                                "/usuarios/*/sesiones/**", "/api/vehiculos/**", "/vehiculos/**")
                        .authenticated()
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/sesiones/consulta", "/sesiones/*").authenticated()
                        .requestMatchers("/", "/usuarios/**", "/sesiones/**", "/css/**", "/js/**")
                        .permitAll()
                        .anyRequest().permitAll())
                .formLogin(formulario -> formulario.disable())
                .httpBasic(basico -> basico.disable())
                .logout(cierre -> cierre.disable())
                .exceptionHandling(errores -> errores.authenticationEntryPoint((request, response, exception) -> {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpStatus.UNAUTHORIZED.value());
                    } else {
                        response.sendRedirect("/usuarios/login");
                    }
                }))
                .sessionManagement(sesion -> sesion.sessionFixation(fijacion -> fijacion.changeSessionId()));
        return http.build();
    }
}
