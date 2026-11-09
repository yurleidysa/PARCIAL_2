package gestion.hospital.yurleidy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/js/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                .requestMatchers("/admin").hasRole("ADMINISTRADOR")
                .requestMatchers("/medico").hasRole("MEDICO")
                .requestMatchers("/paciente").hasRole("PACIENTE")

                // el paciente solo cambia el horario de sus citas
                .requestMatchers(HttpMethod.PATCH, "/api/consultas/*/horario").hasRole("PACIENTE")

                .requestMatchers(HttpMethod.GET, "/api/consultas")
                    .hasAnyRole("ADMINISTRADOR", "MEDICO", "PACIENTE")

                .requestMatchers("/api/consultas/**", "/api/medicos/**").hasRole("ADMINISTRADOR")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("correo")
                .passwordParameter("contrasena")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?salio")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                // la API responde 401 cuando no se ha iniciado sesión
                .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                .accessDeniedPage("/acceso-denegado")
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }
}
