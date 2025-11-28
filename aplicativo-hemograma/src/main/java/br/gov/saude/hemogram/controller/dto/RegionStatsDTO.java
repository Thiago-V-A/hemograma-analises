package br.gov.saude.hemogram.controller.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionStatsDTO {
    private String region;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private Integer totalHemograms;
    private Integer alertedHemograms;
    private Double alertProportion;
    private Double avgLeukocytes;
    private Double avgPlatelets;
    private Double avgHemoglobin;
    private Double leukocytesTrend;
    private Double plateletsTrend;
    private Double hemoglobinTrend;
}