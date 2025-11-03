package com.starwars.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {
    
    private final Environment environment;
    
    public SwaggerConfig(Environment environment) {
        this.environment = environment;
    }
    
    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("Star Wars Challenge API")
                        .version("1.0")
                        .description("API REST para consultar datos de Star Wars con autenticación JWT"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
        
        List<Server> servers = new ArrayList<>();
        
        // Agregar servidor según el perfil activo
        String activeProfile = environment.getProperty("spring.profiles.active", "");
        if (activeProfile.contains("prod")) {
            servers.add(new Server()
                    .url("https://starwars-challenge-production.up.railway.app")
                    .description("Servidor de producción"));
        } else {
            servers.add(new Server()
                    .url("http://localhost:8080")
                    .description("Servidor local"));
        }
        
        openAPI.setServers(servers);
        
        return openAPI;
    }
}




