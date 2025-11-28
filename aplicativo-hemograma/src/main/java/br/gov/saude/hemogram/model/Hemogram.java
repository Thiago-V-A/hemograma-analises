package br.gov.saude.hemogram.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hemograms", indexes = {
        @Index(name = "idx_region_received", columnList = "patient_region,received_at"),
        @Index(name = "idx_fhir_id", columnList = "fhir_id"),
        @Index(name = "idx_received_at", columnList = "received_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hemogram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fhir_id", nullable = false, unique = true, length = 100)
    private String fhirId;

    @Column(name = "patient_region", nullable = false, length = 50)
    private String patientRegion; // Código IBGE ou identificador da região

    @Column(name = "health_unit", length = 100)
    private String healthUnit;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "observation_date")
    private LocalDateTime observationDate;

    // Parâmetros hematológicos
    @Column(name = "leukocytes")
    private Double leukocytes; // /µL

    @Column(name = "platelets")
    private Double platelets; // /µL

    @Column(name = "hemoglobin")
    private Double hemoglobin; // g/dL

    @Column(name = "hematocrit")
    private Double hematocrit; // %

    @Column(name = "neutrophils")
    private Double neutrophils; // %

    @Column(name = "lymphocytes")
    private Double lymphocytes; // %

    @Column(name = "monocytes")
    private Double monocytes; // %

    @Column(name = "eosinophils")
    private Double eosinophils; // %

    @Column(name = "basophils")
    private Double basophils; // %

    // Alertas individuais
    @Column(name = "has_individual_alert")
    private Boolean hasIndividualAlert;

    @Column(name = "alert_parameters", length = 500)
    private String alertParameters; // JSON com parâmetros alterados

    @Column(name = "fhir_raw", columnDefinition = "TEXT")
    private String fhirRaw; // JSON do recurso FHIR completo

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Version
    private Long version;
}