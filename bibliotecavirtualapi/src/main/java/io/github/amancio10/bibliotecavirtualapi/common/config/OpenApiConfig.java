package io.github.amancio10.bibliotecavirtualapi.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Biblioteca Virtual API")
                .version("v1")
                .description("API para gestão de acervo, leitores, empréstimos, reservas e multas."));
    }
}
