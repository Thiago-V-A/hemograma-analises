package br.gov.saude.hemogram.controller.dto;

import br.gov.saude.hemogram.model.AlertSeverity;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private Long id;
    private String alertCode;
    private String region;
    private String parameter;
    private AlertSeverity severity;
    private Integer affectedHemograms;
    private Integer totalHemograms;
    private Double alertProportion;
    private Double currentAvg;
    private Double previousAvg;
    private Double trendPercent;
    private String description;
    private LocalDateTime createdAt;
    private Boolean acknowledged;
    private LocalDateTime acknowledgedAt;
}