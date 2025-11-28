package br.gov.saude.hemogram.service.analytics;

import br.gov.saude.hemogram.model.Hemogram;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HemogramAnalysisServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HemogramAnalysisService service;

    @BeforeEach
    void setUp() {
        // Configurar valores de referência
        ReflectionTestUtils.setField(service, "leukocytesMin", 4000.0);
        ReflectionTestUtils.setField(service, "leukocytesMax", 11000.0);
        ReflectionTestUtils.setField(service, "plateletsMin", 150000.0);
        ReflectionTestUtils.setField(service, "plateletsMax", 450000.0);
    }

    @Test
    void shouldDetectHighLeukocytes() {
        // Given
        Hemogram hemogram = Hemogram.builder()
                .fhirId("test-001")
                .leukocytes(15000.0)  // Valor alto
                .platelets(200000.0)
                .hemoglobin(14.0)
                .build();

        // When
        service.analyzeIndividualHemogram(hemogram);

        // Then
        assertTrue(hemogram.getHasIndividualAlert());
        assertNotNull(hemogram.getAlertParameters());
    }

    @Test
    void shouldDetectLowPlatelets() {
        // Given
        Hemogram hemogram = Hemogram.builder()
                .fhirId("test-002")
                .leukocytes(7000.0)
                .platelets(100000.0)  // Valor baixo
                .hemoglobin(14.0)
                .build();

        // When
        service.analyzeIndividualHemogram(hemogram);

        // Then
        assertTrue(hemogram.getHasIndividualAlert());
    }

    @Test
    void shouldNotAlertNormalValues() {
        // Given
        Hemogram hemogram = Hemogram.builder()
                .fhirId("test-003")
                .leukocytes(7000.0)
                .platelets(250000.0)
                .hemoglobin(14.5)
                .build();

        // When
        service.analyzeIndividualHemogram(hemogram);

        // Then
        assertFalse(hemogram.getHasIndividualAlert());
    }
}