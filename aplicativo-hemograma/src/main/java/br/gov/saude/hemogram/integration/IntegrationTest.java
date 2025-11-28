package br.gov.saude.hemogram.integration;

import br.gov.saude.hemogram.model.Hemogram;
import br.gov.saude.hemogram.repository.HemogramRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HemogramRepository hemogramRepository;

    @Test
    void shouldProcessFullWorkflow() throws Exception {
        // 1. Enviar hemograma via subscription
        String fhirJson = buildTestFhirObservation();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(fhirJson, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/fhir/subscription",
                request,
                Void.class
        );

        assertEquals(200, response.getStatusCode().value());

        // 2. Verificar se foi salvo no banco
        Thread.sleep(1000); // Aguardar processamento assíncrono

        Hemogram saved = hemogramRepository.findByFhirId("test-integration-001")
                .orElseThrow();

        assertNotNull(saved);
        assertEquals("3550308", saved.getPatientRegion());
        assertTrue(saved.getLeukocytes() > 0);

        // 3. Verificar alertas (se aplicável)
        if (saved.getLeukocytes() > 11000) {
            assertTrue(saved.getHasIndividualAlert());
        }
    }

    private String buildTestFhirObservation() {
        return """
            {
              "resourceType": "Observation",
              "id": "test-integration-001",
              "status": "final",
              "code": {
                "coding": [{
                  "system": "http://loinc.org",
                  "code": "58410-2"
                }]
              },
              "extension": [{
                "url": "http://saude.gov.br/fhir/StructureDefinition/patient-region",
                "valueString": "3550308"
              }],
              "component": [
                {
                  "code": {"coding": [{"code": "6690-2"}]},
                  "valueQuantity": {"value": 12000}
                },
                {
                  "code": {"coding": [{"code": "777-3"}]},
                  "valueQuantity": {"value": 200000}
                }
              ]
            }
            """;
    }
}