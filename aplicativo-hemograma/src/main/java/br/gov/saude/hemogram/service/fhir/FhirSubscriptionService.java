package br.gov.saude.hemogram.service.fhir;

import br.gov.saude.hemogram.model.Hemogram;
import br.gov.saude.hemogram.repository.HemogramRepository;
import br.gov.saude.hemogram.service.analytics.HemogramAnalysisService;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FhirSubscriptionService {

    private final HemogramRepository hemogramRepository;
    private final HemogramAnalysisService analysisService;
    private final FhirContext fhirContext;
    private final ObjectMapper objectMapper;

    @Transactional
    public void processObservation(String fhirJson) {
        try {
            log.debug("Processando Observation FHIR recebida");

            IParser parser = fhirContext.newJsonParser();
            Observation observation = parser.parseResource(Observation.class, fhirJson);

            // Verificar se já foi processado
            String fhirId = observation.getIdElement().getIdPart();
            if (hemogramRepository.findByFhirId(fhirId).isPresent()) {
                log.info("Hemograma {} já foi processado anteriormente", fhirId);
                return;
            }

            // Extrair dados do hemograma
            Hemogram hemogram = extractHemogramData(observation, fhirJson);

            // Analisar hemograma individual
            analysisService.analyzeIndividualHemogram(hemogram);

            // Salvar hemograma
            hemogram = hemogramRepository.save(hemogram);
            log.info("Hemograma {} processado e salvo com sucesso", fhirId);

            // Notificar sistema de análise coletiva (assíncrono)
            analysisService.notifyNewHemogram(hemogram);

        } catch (Exception e) {
            log.error("Erro ao processar Observation FHIR", e);
            throw new RuntimeException("Falha no processamento do hemograma", e);
        }
    }

    private Hemogram extractHemogramData(Observation observation, String fhirJson) {
        Hemogram.HemogramBuilder builder = Hemogram.builder()
                .fhirId(observation.getIdElement().getIdPart())
                .fhirRaw(fhirJson)
                .receivedAt(LocalDateTime.now())
                .processedAt(LocalDateTime.now());

        // Data da observação
        if (observation.hasEffectiveDateTimeType()) {
            Date effectiveDate = observation.getEffectiveDateTimeType().getValue();
            builder.observationDate(
                    LocalDateTime.ofInstant(effectiveDate.toInstant(), ZoneId.systemDefault())
            );
        }

        // Extrair região do paciente
        String region = extractPatientRegion(observation);
        builder.patientRegion(region);

        // Extrair unidade de saúde
        String healthUnit = extractHealthUnit(observation);
        builder.healthUnit(healthUnit);

        // Processar componentes do hemograma
        if (observation.hasComponent()) {
            for (Observation.ObservationComponentComponent component : observation.getComponent()) {
                extractComponentValue(component, builder);
            }
        }

        // Se for uma Observation simples (não agrupada), extrair valor direto
        if (observation.hasValue()) {
            extractDirectValue(observation, builder);
        }

        return builder.build();
    }

    private String extractPatientRegion(Observation observation) {
        // Extrair da extensão ou do subject
        if (observation.hasExtension()) {
            for (Extension ext : observation.getExtension()) {
                if (ext.getUrl().contains("patient-region") ||
                        ext.getUrl().contains("municipality")) {
                    if (ext.getValue() instanceof StringType) {
                        return ((StringType) ext.getValue()).getValue();
                    } else if (ext.getValue() instanceof CodeableConcept) {
                        CodeableConcept cc = (CodeableConcept) ext.getValue();
                        if (cc.hasCoding() && !cc.getCoding().isEmpty()) {
                            return cc.getCoding().get(0).getCode();
                        }
                    }
                }
            }
        }

        // Fallback: extrair do performer ou location
        if (observation.hasPerformer()) {
            Reference performer = observation.getPerformer().get(0);
            if (performer.hasIdentifier()) {
                Identifier identifier = performer.getIdentifier();
                if (identifier.hasValue()) {
                    return identifier.getValue();
                }
            }
        }

        return "UNKNOWN";
    }

    private String extractHealthUnit(Observation observation) {
        if (observation.hasPerformer()) {
            for (Reference performer : observation.getPerformer()) {
                if (performer.hasDisplay()) {
                    return performer.getDisplay();
                }
            }
        }
        return null;
    }

    private void extractComponentValue(
            Observation.ObservationComponentComponent component,
            Hemogram.HemogramBuilder builder
    ) {
        if (!component.hasCode() || !component.hasValue()) {
            return;
        }

        String code = getCodeFromCodeableConcept(component.getCode());
        Double value = extractNumericValue(component.getValue());

        if (value == null) {
            return;
        }

        // Mapear código LOINC para campo correspondente
        switch (code) {
            case "6690-2": // Leucócitos
            case "WBC":
                builder.leukocytes(value);
                break;
            case "777-3": // Plaquetas
            case "PLT":
                builder.platelets(value);
                break;
            case "718-7": // Hemoglobina
            case "HGB":
                builder.hemoglobin(value);
                break;
            case "4544-3": // Hematócrito
            case "HCT":
                builder.hematocrit(value);
                break;
            case "770-8": // Neutrófilos
            case "NEUT":
                builder.neutrophils(value);
                break;
            case "736-9": // Linfócitos
            case "LYMPH":
                builder.lymphocytes(value);
                break;
            case "5905-5": // Monócitos
            case "MONO":
                builder.monocytes(value);
                break;
            case "713-8": // Eosinófilos
            case "EOS":
                builder.eosinophils(value);
                break;
            case "706-2": // Basófilos
            case "BASO":
                builder.basophils(value);
                break;
        }
    }

    private void extractDirectValue(Observation observation, Hemogram.HemogramBuilder builder) {
        if (!observation.hasCode()) {
            return;
        }

        String code = getCodeFromCodeableConcept(observation.getCode());
        Double value = extractNumericValue(observation.getValue());

        if (value != null) {
            // Usar mesma lógica de mapeamento
            Observation.ObservationComponentComponent dummy =
                    new Observation.ObservationComponentComponent()
                            .setCode(observation.getCode())
                            .setValue(observation.getValue());
            extractComponentValue(dummy, builder);
        }
    }

    private String getCodeFromCodeableConcept(CodeableConcept codeableConcept) {
        if (codeableConcept.hasCoding()) {
            for (Coding coding : codeableConcept.getCoding()) {
                if (coding.hasCode()) {
                    return coding.getCode();
                }
            }
        }
        return null;
    }

    private Double extractNumericValue(Type value) {
        if (value instanceof Quantity) {
            Quantity quantity = (Quantity) value;
            if (quantity.hasValue()) {
                return quantity.getValue().doubleValue();
            }
        } else if (value instanceof IntegerType) {
            return ((IntegerType) value).getValue().doubleValue();
        } else if (value instanceof DecimalType) {
            return ((DecimalType) value).getValue().doubleValue();
        }
        return null;
    }
}

