package br.com.milhas.gerenciador.config;
// Classe responsável por configurar a documentação OpenAPI da aplicação.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SpringDocConfig {

    @Bean /* Configura a documentação OpenAPI com informações da API e esquema de segurança JWT */
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-key"))
                .info(new Info()
                        .title("Gerenciador de Milhas API")
                        .description("API Rest da aplicação de gerenciamento de milhas e pontos.")
                        .contact(new Contact()
                                .name("Time de Desenvolvimento")
                                .email("welber@email.com"))
                        .version("1.0.0"));
    }
}
