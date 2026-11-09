package gestion.hospital.yurleidy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiConsultas() {
        return new OpenAPI().info(new Info()
                .title("API Gestión de Consultas Médicas")
                .version("1.0")
                .description("Gestión de consultas médicas. "
                        + "Roles: ADMINISTRADOR crea, edita y elimina médicos y asigna consultorios, "
                        + "MEDICO solo visualiza las consultas asignadas a él, "
                        + "PACIENTE visualiza y actualiza el horario solo de sus citas. "
                        + "Para probar, primero inicie sesión en http://localhost:8080/login en el mismo navegador. "
                       ));
    }
}

