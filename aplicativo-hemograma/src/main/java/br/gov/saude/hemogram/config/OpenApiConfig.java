package br.gov.saude.hemogram.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Monitoramento de Hemogramas")
                        .version("1.0.0")
                        .description("API para vigilância epidemiológica baseada em hemogramas FHIR")
                        .contact(new Contact()
                                .name("Secretaria de Saúde")
                                .email("vigilancia@saude.gov.br"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}