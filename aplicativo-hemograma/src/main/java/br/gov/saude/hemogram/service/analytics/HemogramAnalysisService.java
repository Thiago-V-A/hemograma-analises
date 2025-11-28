package br.gov.saude.hemogram.service.analytics;

import br.gov.saude.hemogram.model.Hemogram;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class HemogramAnalysisService {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${analytics.reference-values.leukocytes.min}")
    private Double leukocytesMin;

    @Value("${analytics.reference-values.leukocytes.max}")
    private Double leukocytesMax;

    @Value("${analytics.reference-values.platelets.min}")
    private Double plateletsMin;

    @Value("${analytics.reference-values.platelets.max}")
    private Double plateletsMax;

    @Value("${analytics.reference-values.hemoglobin.male-min}")
    private Double hemoglobinMaleMin;

    @Value("${analytics.reference-values.hemoglobin.male-max}")
    private Double hemoglobinMaleMax;

    @Value("${analytics.reference-values.hemoglobin.female-min}")
    private Double hemoglobinFemaleMin;

    @Value("${analytics.reference-values.hemoglobin.female-max}")
    private Double hemoglobinFemaleMax;

    public void analyzeIndividualHemogram(Hemogram hemogram) {
        log.debug("Analisando hemograma individual: {}", hemogram.getFhirId());

        List<String> alertParameters = new ArrayList<>();

        // Verificar leucócitos
        if (hemogram.getLeukocytes() != null) {
            if (hemogram.getLeukocytes() < leukocytesMin) {
                alertParameters.add("leucocitos_baixos:" + hemogram.getLeukocytes());
            } else if (hemogram.getLeukocytes() > leukocytesMax) {
                alertParameters.add("leucocitos_altos:" + hemogram.getLeukocytes());
            }
        }

        // Verificar plaquetas
        if (hemogram.getPlatelets() != null) {
            if (hemogram.getPlatelets() < plateletsMin) {
                alertParameters.add("plaquetas_baixas:" + hemogram.getPlatelets());
            } else if (hemogram.getPlatelets() > plateletsMax) {
                alertParameters.add("plaquetas_altas:" + hemogram.getPlatelets());
            }
        }

        // Verificar hemoglobina (usando valores masculinos como padrão)
        if (hemogram.getHemoglobin() != null) {
            if (hemogram.getHemoglobin() < hemoglobinFemaleMin) {
                alertParameters.add("hemoglobina_baixa:" + hemogram.getHemoglobin());
            } else if (hemogram.getHemoglobin() > hemoglobinMaleMax) {
                alertParameters.add("hemoglobina_alta:" + hemogram.getHemoglobin());
            }
        }

        // Verificar neutrófilos
        if (hemogram.getNeutrophils() != null) {
            if (hemogram.getNeutrophils() < 40 || hemogram.getNeutrophils() > 70) {
                alertParameters.add("neutrofilos_alterados:" + hemogram.getNeutrophils());
            }
        }

        // Verificar linfócitos
        if (hemogram.getLymphocytes() != null) {
            if (hemogram.getLymphocytes() < 20 || hemogram.getLymphocytes() > 45) {
                alertParameters.add("linfocitos_alterados:" + hemogram.getLymphocytes());
            }
        }

        // Definir se há alerta
        hemogram.setHasIndividualAlert(!alertParameters.isEmpty());

        if (!alertParameters.isEmpty()) {
            try {
                hemogram.setAlertParameters(objectMapper.writeValueAsString(alertParameters));
                log.info("Hemograma {} apresenta alertas: {}",
                        hemogram.getFhirId(), alertParameters);
            } catch (Exception e) {
                log.error("Erro ao serializar parâmetros de alerta", e);
            }
        }
    }

    public void notifyNewHemogram(Hemogram hemogram) {
        // Publicar evento para análise coletiva assíncrona
        eventPublisher.publishEvent(new NewHemogramEvent(this, hemogram));
    }

    // Evento interno
    public static class NewHemogramEvent extends org.springframework.context.ApplicationEvent {
        private final Hemogram hemogram;

        public NewHemogramEvent(Object source, Hemogram hemogram) {
            super(source);
            this.hemogram = hemogram;
        }

        public Hemogram getHemogram() {
            return hemogram;
        }
    }
}