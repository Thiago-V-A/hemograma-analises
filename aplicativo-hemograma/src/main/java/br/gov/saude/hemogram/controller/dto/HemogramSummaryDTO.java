package br.gov.saude.hemogram.controller.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HemogramSummaryDTO {
    private Long id;
    private String fhirId;
    private String region;
    private LocalDateTime observationDate;
    private Double leukocytes;
    private Double platelets;
    private Double hemoglobin;
    private Boolean hasAlert;
}