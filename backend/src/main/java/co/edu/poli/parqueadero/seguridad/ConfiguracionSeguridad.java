package co.edu.poli.parqueadero.seguridad;

import co.edu.poli.parqueadero.dao.UsuarioDAO;
import co.edu.poli.parqueadero.modelo.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ConfiguracionSeguridad {

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/vehiculos/**", "/usuarios/**", "/admin/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/usuarios/registro").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/error", "/recuperar-contrasena",
                                "/restablecer-contrasena").permitAll()
                        .requestMatchers("/administracion/**").hasRole("ADMIN")
                        .requestMatchers("/mis-vehiculos/**").authenticated()
                        .requestMatchers("/sesiones/**").authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form.loginPage("/login")
                        .defaultSuccessUrl("/sesiones/entrada", true)
                        .permitAll())
                .logout(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(UsuarioDAO usuarioDAO) {
        return correo -> {
            Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
            if (usuario == null) {
                throw new UsernameNotFoundException("Usuario no encontrado");
            }
            String rol = usuario.getRol() == null || usuario.getRol().isBlank()
                    ? "USER"
                    : usuario.getRol().toUpperCase();
            return User.withUsername(usuario.getCorreo())
                    .password(usuario.getPasswordHash())
                    .roles(rol)
                    .build();
        };
    }

}
