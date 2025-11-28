package br.gov.saude.hemogram.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "collective_alerts", indexes = {
        @Index(name = "idx_region_created", columnList = "region,created_at"),
        @Index(name = "idx_severity", columnList = "severity"),
        @Index(name = "idx_acknowledged", columnList = "acknowledged")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectiveAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_code", nullable = false, unique = true, length = 50)
    private String alertCode; // Ex: ALERT-20250101-SP-LEU-001

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "parameter", nullable = false, length = 50)
    private String parameter; // leukocytes, platelets, hemoglobin

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private AlertSeverity severity;

    @Column(name = "affected_hemograms", nullable = false)
    private Integer affectedHemograms;

    @Column(name = "total_hemograms", nullable = false)
    private Integer totalHemograms;

    @Column(name = "alert_proportion")
    private Double alertProportion;

    // Valores estatísticos
    @Column(name = "current_avg")
    private Double currentAvg;

    @Column(name = "previous_avg")
    private Double previousAvg;

    @Column(name = "trend_percent")
    private Double trendPercent;

    @Column(name = "std_deviation")
    private Double stdDeviation;

    @Column(name = "window_start")
    private LocalDateTime windowStart;

    @Column(name = "window_end")
    private LocalDateTime windowEnd;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(name = "fhir_communication_id", length = 100)
    private String fhirCommunicationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "acknowledged")
    private Boolean acknowledged;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "acknowledged_by", length = 100)
    private String acknowledgedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_stats_id")
    private RegionStats regionStats;

    @Version
    private Long version;
}
