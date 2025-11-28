package br.gov.saude.hemogram.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "region_stats", indexes = {
        @Index(name = "idx_region_window", columnList = "region,window_start,window_end"),
        @Index(name = "idx_window_end", columnList = "window_end")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "region", nullable = false, length = 50)
    private String region;

    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "total_hemograms", nullable = false)
    private Integer totalHemograms;

    @Column(name = "alerted_hemograms")
    private Integer alertedHemograms;

    @Column(name = "alert_proportion")
    private Double alertProportion;

    // Estatísticas de leucócitos
    @Column(name = "avg_leukocytes")
    private Double avgLeukocytes;

    @Column(name = "std_leukocytes")
    private Double stdLeukocytes;

    @Column(name = "leukocytes_trend")
    private Double leukocytesTrend; // % mudança em relação à janela anterior

    // Estatísticas de plaquetas
    @Column(name = "avg_platelets")
    private Double avgPlatelets;

    @Column(name = "std_platelets")
    private Double stdPlatelets;

    @Column(name = "platelets_trend")
    private Double plateletsTrend;

    // Estatísticas de hemoglobina
    @Column(name = "avg_hemoglobin")
    private Double avgHemoglobin;

    @Column(name = "std_hemoglobin")
    private Double stdHemoglobin;

    @Column(name = "hemoglobin_trend")
    private Double hemoglobinTrend;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "previous_stats_id")
    private Long previousStatsId; // Referência para estatísticas da janela anterior

    @Version
    private Long version;
}
