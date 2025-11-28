package br.gov.saude.hemogram.controller.dto;

import br.gov.saude.hemogram.model.AlertSeverity;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AlertDetailDTO {
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
    private Double stdDeviation;
    private LocalDateTime windowStart;
    private LocalDateTime windowEnd;
    private String description;
    private String recommendedAction;
    private LocalDateTime createdAt;
    private Boolean acknowledged;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private String notes;
    private List<HemogramSummaryDTO> affectedHemograms;
}