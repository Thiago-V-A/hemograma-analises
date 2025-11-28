// ============ FhirSubscriptionControllerTest.java ============
package br.gov.saude.hemogram.controller;

import br.gov.saude.hemogram.service.fhir.FhirSubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FhirSubscriptionController.class)
class FhirSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FhirSubscriptionService fhirSubscriptionService;

    @Test
    void shouldReceiveSubscription() throws Exception {
        // Given
        String fhirJson = """
            {
              "resourceType": "Observation",
              "id": "hemogram-001",
              "status": "final",
              "code": {
                "coding": [{
                  "system": "http://loinc.org",
                  "code": "58410-2",
                  "display": "Complete blood count"
                }]
              },
              "component": [
                {
                  "code": {
                    "coding": [{
                      "system": "http://loinc.org",
                      "code": "6690-2",
                      "display": "Leukocytes"
                    }]
                  },
                  "valueQuantity": {
                    "value": 12000,
                    "unit": "/uL"
                  }
                }
              ]
            }
            """;

        doNothing().when(fhirSubscriptionService).processObservation(anyString());

        // When & Then
        mockMvc.perform(post("/api/fhir/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fhirJson))
                .andExpect(status().isOk());
    }
}