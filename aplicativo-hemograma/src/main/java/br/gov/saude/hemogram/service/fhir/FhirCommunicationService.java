package br.gov.saude.hemogram.service.fhir;

import br.gov.saude.hemogram.model.CollectiveAlert;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class FhirCommunicationService {

    private final FhirContext fhirContext;

    @Value("${fhir.server.url}")
    private String fhirServerUrl;

    public String createCommunicationForAlert(CollectiveAlert alert) {
        try {
            log.debug("Criando recurso Communication para alerta {}", alert.getAlertCode());

            Communication communication = new Communication();
            communication.setStatus(Communication.CommunicationStatus.COMPLETED);

            // Categoria: alerta epidemiológico
            CodeableConcept category = new CodeableConcept();
            category.addCoding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/communication-category")
                    .setCode("alert")
                    .setDisplay("Alert");
            communication.addCategory(category);

            // Prioridade baseada na severidade
            Communication.CommunicationPriority priority = mapSeverityToPriority(alert.getSeverity());
            communication.setPriority(priority);

            // Assunto
            communication.addAbout()
                    .setDisplay("Alerta Coletivo: " + alert.getParameter() + " - " + alert.getRegion());

            // Payload com informações do alerta
            Communication.CommunicationPayloadComponent payload =
                    new Communication.CommunicationPayloadComponent();

            String content = buildAlertContent(alert);
            payload.setContent(new StringType(content));
            communication.addPayload(payload);

            // Data de envio
            communication.setSent(Date.from(alert.getCreatedAt()
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()));

            // Enviar para servidor FHIR
            IGenericClient client = fhirContext.newRestfulGenericClient(fhirServerUrl);
            MethodOutcome outcome = client.create()
                    .resource(communication)
                    .execute();

            String communicationId = outcome.getId().getIdPart();
            log.info("Communication {} criado com sucesso para alerta {}",
                    communicationId, alert.getAlertCode());

            return communicationId;

        } catch (Exception e) {
            log.error("Erro ao criar Communication para alerta {}", alert.getAlertCode(), e);
            return null;
        }
    }

    private Communication.CommunicationPriority mapSeverityToPriority(
            br.gov.saude.hemogram.model.AlertSeverity severity
    ) {
        return switch (severity) {
            case CRITICAL -> Communication.CommunicationPriority.STAT;
            case HIGH -> Communication.CommunicationPriority.URGENT;
            case MEDIUM -> Communication.CommunicationPriority.ASAP;
            case LOW -> Communication.CommunicationPriority.ROUTINE;
        };
    }

    private String buildAlertContent(CollectiveAlert alert) {
        return String.format("""
            ALERTA COLETIVO DETECTADO
            
            Código: %s
            Região: %s
            Parâmetro: %s
            Severidade: %s
            
            Hemogramas afetados: %d de %d (%.1f%%)
            Valor médio atual: %.2f
            Valor médio anterior: %.2f
            Tendência: %+.1f%%
            
            Janela de tempo: %s a %s
            
            Descrição: %s
            
            Ação recomendada: %s
            """,
                alert.getAlertCode(),
                alert.getRegion(),
                alert.getParameter(),
                alert.getSeverity(),
                alert.getAffectedHemograms(),
                alert.getTotalHemograms(),
                alert.getAlertProportion() * 100,
                alert.getCurrentAvg(),
                alert.getPreviousAvg(),
                alert.getTrendPercent(),
                alert.getWindowStart(),
                alert.getWindowEnd(),
                alert.getDescription(),
                alert.getRecommendedAction()
        );
    }
}