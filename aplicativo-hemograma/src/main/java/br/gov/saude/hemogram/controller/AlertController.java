package br.gov.saude.hemogram.controller;

import br.gov.saude.hemogram.controller.dto.*;
import br.gov.saude.hemogram.model.*;
import br.gov.saude.hemogram.repository.CollectiveAlertRepository;
import br.gov.saude.hemogram.repository.HemogramRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Gerenciamento de alertas coletivos")
public class AlertController {

    private final CollectiveAlertRepository alertRepository;
    private final HemogramRepository hemogramRepository;

    @GetMapping
    @Operation(summary = "Lista alertas coletivos com filtros")
    public ResponseEntity<Page<AlertDTO>> listAlerts(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) AlertSeverity severity,
            @RequestParam(required = false) Boolean acknowledged,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        LocalDateTime start = dateFrom != null ? dateFrom : LocalDateTime.now().minusDays(30);
        LocalDateTime end = dateTo != null ? dateTo : LocalDateTime.now();

        Page<CollectiveAlert> alerts = alertRepository.findByFilters(
                region, severity, acknowledged, start, end, pageable
        );

        Page<AlertDTO> dtos = alerts.map(this::toDTO);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém detalhes de um alerta")
    public ResponseEntity<AlertDetailDTO> getAlert(@PathVariable Long id) {
        return alertRepository.findById(id)
                .map(alert -> {
                    AlertDetailDTO dto = toDetailDTO(alert);

                    // Buscar hemogramas afetados
                    List<Hemogram> affectedHemograms = hemogramRepository
                            .findAlertedHemogramsByRegionAndWindow(
                                    alert.getRegion(),
                                    alert.getWindowStart(),
                                    alert.getWindowEnd()
                            );

                    dto.setAffectedHemograms(affectedHemograms.stream()
                            .map(this::toHemogramSummaryDTO)
                            .toList());

                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/acknowledge")
    @Operation(summary = "Marca um alerta como reconhecido")
    public ResponseEntity<AlertDTO> acknowledgeAlert(
            @PathVariable Long id,
            @RequestBody AcknowledgeRequest request
    ) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.setAcknowledged(true);
                    alert.setAcknowledgedAt(LocalDateTime.now());
                    alert.setAcknowledgedBy(request.getUserId());
                    alert.setNotes(request.getNotes());

                    CollectiveAlert saved = alertRepository.save(alert);
                    return ResponseEntity.ok(toDTO(saved));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/unacknowledged")
    @Operation(summary = "Lista alertas não reconhecidos")
    public ResponseEntity<List<AlertDTO>> getUnacknowledgedAlerts() {
        List<CollectiveAlert> alerts = alertRepository.findUnacknowledgedAlerts();
        return ResponseEntity.ok(alerts.stream().map(this::toDTO).toList());
    }

    @GetMapping("/recent")
    @Operation(summary = "Obtém os 10 alertas mais recentes")
    public ResponseEntity<List<AlertDTO>> getRecentAlerts() {
        List<CollectiveAlert> alerts = alertRepository.findTop10ByOrderByCreatedAtDesc();
        return ResponseEntity.ok(alerts.stream().map(this::toDTO).toList());
    }

    private AlertDTO toDTO(CollectiveAlert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .alertCode(alert.getAlertCode())
                .region(alert.getRegion())
                .parameter(alert.getParameter())
                .severity(alert.getSeverity())
                .affectedHemograms(alert.getAffectedHemograms())
                .totalHemograms(alert.getTotalHemograms())
                .alertProportion(alert.getAlertProportion())
                .currentAvg(alert.getCurrentAvg())
                .previousAvg(alert.getPreviousAvg())
                .trendPercent(alert.getTrendPercent())
                .description(alert.getDescription())
                .createdAt(alert.getCreatedAt())
                .acknowledged(alert.getAcknowledged())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .build();
    }

    private AlertDetailDTO toDetailDTO(CollectiveAlert alert) {
        AlertDetailDTO dto = new AlertDetailDTO();
        dto.setId(alert.getId());
        dto.setAlertCode(alert.getAlertCode());
        dto.setRegion(alert.getRegion());
        dto.setParameter(alert.getParameter());
        dto.setSeverity(alert.getSeverity());
        dto.setAffectedHemograms(alert.getAffectedHemograms());
        dto.setTotalHemograms(alert.getTotalHemograms());
        dto.setAlertProportion(alert.getAlertProportion());
        dto.setCurrentAvg(alert.getCurrentAvg());
        dto.setPreviousAvg(alert.getPreviousAvg());
        dto.setTrendPercent(alert.getTrendPercent());
        dto.setStdDeviation(alert.getStdDeviation());
        dto.setWindowStart(alert.getWindowStart());
        dto.setWindowEnd(alert.getWindowEnd());
        dto.setDescription(alert.getDescription());
        dto.setRecommendedAction(alert.getRecommendedAction());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setAcknowledged(alert.getAcknowledged());
        dto.setAcknowledgedBy(alert.getAcknowledgedBy());
        dto.setNotes(alert.getNotes());
        return dto;
    }

    private HemogramSummaryDTO toHemogramSummaryDTO(Hemogram h) {
        return HemogramSummaryDTO.builder()
                .id(h.getId())
                .fhirId(h.getFhirId())
                .region(h.getPatientRegion())
                .observationDate(h.getObservationDate())
                .leukocytes(h.getLeukocytes())
                .platelets(h.getPlatelets())
                .hemoglobin(h.getHemoglobin())
                .hasAlert(h.getHasIndividualAlert())
                .build();
    }
}