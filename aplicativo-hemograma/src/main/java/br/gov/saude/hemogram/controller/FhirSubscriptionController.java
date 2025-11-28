package br.gov.saude.hemogram.controller;

import br.gov.saude.hemogram.service.fhir.FhirSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fhir")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "FHIR", description = "Endpoints para recebimento de recursos FHIR")
public class FhirSubscriptionController {

    private final FhirSubscriptionService fhirSubscriptionService;

    @PostMapping("/subscription")
    @Operation(summary = "Recebe notificações de subscription FHIR")
    public ResponseEntity<Void> receiveSubscription(@RequestBody String fhirJson) {
        log.info("Subscription recebida");

        try {
            fhirSubscriptionService.processObservation(fhirJson);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erro ao processar subscription", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}





