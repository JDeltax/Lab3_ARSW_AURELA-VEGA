package edu.eci.arsw.blueprints.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blueprints REST API")
                        .version("1.0.0")
                        .description("API REST para la gestión y consulta de planos arquitectónicos (Blueprints)")
                        .contact(new Contact()
                                .name("Escuela Colombiana de Ingeniería Julio Garavito")));
    }
}
