package br.gov.saude.hemogram.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirConfig {

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    @Value("${fhir.server.timeout}")
    private Integer timeout;

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR4();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        IGenericClient client = fhirContext.newRestfulGenericClient(fhirServerUrl);
        client.setConnectionTimeout(timeout);
        client.setSocketTimeout(timeout);
        return client;
    }
}