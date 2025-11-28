package br.gov.saude.hemogram.controller;

import br.gov.saude.hemogram.controller.dto.RegionStatsDTO;
import br.gov.saude.hemogram.repository.RegionStatsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Estatísticas", description = "Estatísticas regionais")
public class StatsController {

    private final RegionStatsRepository statsRepository;

    @GetMapping("/regions")
    @Operation(summary = "Lista estatísticas por região")
    public ResponseEntity<List<String>> listRegions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateTo
    ) {
        LocalDateTime start = dateFrom != null ? dateFrom : LocalDateTime.now().minusDays(7);
        LocalDateTime end = dateTo != null ? dateTo : LocalDateTime.now();

        List<String> regions = statsRepository.findRegionsWithStats(start, end);
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/regions/{region}")
    @Operation(summary = "Obtém estatísticas de uma região")
    public ResponseEntity<List<RegionStatsDTO>> getRegionStats(
            @PathVariable String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateTo
    ) {
        LocalDateTime start = dateFrom != null ? dateFrom : LocalDateTime.now().minusDays(7);
        LocalDateTime end = dateTo != null ? dateTo : LocalDateTime.now();

        var stats = statsRepository.findByRegionAndWindowEndBetween(region, start, end);

        List<RegionStatsDTO> dtos = stats.stream()
                .map(s -> RegionStatsDTO.builder()
                        .region(s.getRegion())
                        .windowStart(s.getWindowStart())
                        .windowEnd(s.getWindowEnd())
                        .totalHemograms(s.getTotalHemograms())
                        .alertedHemograms(s.getAlertedHemograms())
                        .alertProportion(s.getAlertProportion())
                        .avgLeukocytes(s.getAvgLeukocytes())
                        .avgPlatelets(s.getAvgPlatelets())
                        .avgHemoglobin(s.getAvgHemoglobin())
                        .leukocytesTrend(s.getLeukocytesTrend())
                        .plateletsTrend(s.getPlateletsTrend())
                        .hemoglobinTrend(s.getHemoglobinTrend())
                        .build())
                .toList();

        return ResponseEntity.ok(dtos);
    }
}