package br.gov.saude.hemogram.service.analytics;

import br.gov.saude.hemogram.model.*;
import br.gov.saude.hemogram.repository.*;
import br.gov.saude.hemogram.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PatternDetectionService {

    private final HemogramRepository hemogramRepository;
    private final RegionStatsRepository regionStatsRepository;
    private final CollectiveAlertRepository collectiveAlertRepository;
    private final NotificationService notificationService;

    @Value("${analytics.window.size-hours}")
    private Integer windowSizeHours;

    @Value("${analytics.thresholds.min-hemograms}")
    private Integer minHemograms;

    @Value("${analytics.thresholds.alert-proportion}")
    private Double alertProportionThreshold;

    @Value("${analytics.thresholds.trend-increase-percent}")
    private Double trendIncreaseThreshold;

    @Scheduled(fixedDelayString = "${analytics.window.evaluation-interval-minutes}",
            timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    @Transactional
    public void evaluateCollectivePatterns() {
        log.info("Iniciando avaliação de padrões coletivos");

        LocalDateTime windowEnd = LocalDateTime.now();
        LocalDateTime windowStart = windowEnd.minusHours(windowSizeHours);

        // Obter regiões ativas na janela
        List<String> activeRegions = hemogramRepository.findActiveRegions(windowStart, windowEnd);
        log.info("Avaliando {} regiões ativas", activeRegions.size());

        for (String region : activeRegions) {
            evaluateRegion(region, windowStart, windowEnd);
        }

        log.info("Avaliação de padrões coletivos concluída");
    }

    private void evaluateRegion(String region, LocalDateTime windowStart, LocalDateTime windowEnd) {
        log.debug("Avaliando região: {}", region);

        // Calcular estatísticas
        RegionStats stats = calculateRegionStats(region, windowStart, windowEnd);

        // Verificar se há dados suficientes
        if (stats.getTotalHemograms() < minHemograms) {
            log.debug("Região {} não possui dados suficientes ({} < {})",
                    region, stats.getTotalHemograms(), minHemograms);
            return;
        }

        // Salvar estatísticas
        stats = regionStatsRepository.save(stats);

        // Detectar padrões anômalos
        detectAnomalies(stats);
    }

    private RegionStats calculateRegionStats(
            String region,
            LocalDateTime windowStart,
            LocalDateTime windowEnd
    ) {
        // Contar hemogramas
        Integer totalHemograms = hemogramRepository.countByRegionAndWindow(
                region, windowStart, windowEnd
        );

        Integer alertedHemograms = hemogramRepository.countAlertedByRegionAndWindow(
                region, windowStart, windowEnd
        );

        Double alertProportion = totalHemograms > 0
                ? (double) alertedHemograms / totalHemograms
                : 0.0;

        // Calcular médias
        Double avgLeukocytes = hemogramRepository.calculateAvgLeukocytes(
                region, windowStart, windowEnd
        );
        Double avgPlatelets = hemogramRepository.calculateAvgPlatelets(
                region, windowStart, windowEnd
        );
        Double avgHemoglobin = hemogramRepository.calculateAvgHemoglobin(
                region, windowStart, windowEnd
        );

        // Calcular desvios padrão
        List<Hemogram> hemograms = hemogramRepository
                .findByPatientRegionAndReceivedAtBetween(region, windowStart, windowEnd);

        DescriptiveStatistics leukStats = new DescriptiveStatistics();
        DescriptiveStatistics platStats = new DescriptiveStatistics();
        DescriptiveStatistics hgbStats = new DescriptiveStatistics();

        for (Hemogram h : hemograms) {
            if (h.getLeukocytes() != null) leukStats.addValue(h.getLeukocytes());
            if (h.getPlatelets() != null) platStats.addValue(h.getPlatelets());
            if (h.getHemoglobin() != null) hgbStats.addValue(h.getHemoglobin());
        }

        // Buscar estatísticas anteriores para calcular tendência
        Optional<RegionStats> previousStats = regionStatsRepository
                .findFirstByRegionAndWindowEndBeforeOrderByWindowEndDesc(region, windowStart);

        Double leukocytesTrend = calculateTrend(avgLeukocytes,
                previousStats.map(RegionStats::getAvgLeukocytes).orElse(null));
        Double plateletsTrend = calculateTrend(avgPlatelets,
                previousStats.map(RegionStats::getAvgPlatelets).orElse(null));
        Double hemoglobinTrend = calculateTrend(avgHemoglobin,
                previousStats.map(RegionStats::getAvgHemoglobin).orElse(null));

        return RegionStats.builder()
                .region(region)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .totalHemograms(totalHemograms)
                .alertedHemograms(alertedHemograms)
                .alertProportion(alertProportion)
                .avgLeukocytes(avgLeukocytes)
                .stdLeukocytes(leukStats.getStandardDeviation())
                .leukocytesTrend(leukocytesTrend)
                .avgPlatelets(avgPlatelets)
                .stdPlatelets(platStats.getStandardDeviation())
                .plateletsTrend(plateletsTrend)
                .avgHemoglobin(avgHemoglobin)
                .stdHemoglobin(hgbStats.getStandardDeviation())
                .hemoglobinTrend(hemoglobinTrend)
                .calculatedAt(LocalDateTime.now())
                .previousStatsId(previousStats.map(RegionStats::getId).orElse(null))
                .build();
    }

    private Double calculateTrend(Double currentValue, Double previousValue) {
        if (currentValue == null || previousValue == null || previousValue == 0) {
            return null;
        }
        return ((currentValue - previousValue) / previousValue) * 100;
    }

    private void detectAnomalies(RegionStats stats) {
        // Verificar leucócitos
        checkParameterAnomaly(stats, "leukocytes",
                stats.getAvgLeukocytes(),
                stats.getLeukocytesTrend(),
                stats.getStdLeukocytes());

        // Verificar plaquetas
        checkParameterAnomaly(stats, "platelets",
                stats.getAvgPlatelets(),
                stats.getPlateletsTrend(),
                stats.getStdPlatelets());

        // Verificar hemoglobina
        checkParameterAnomaly(stats, "hemoglobin",
                stats.getAvgHemoglobin(),
                stats.getHemoglobinTrend(),
                stats.getStdHemoglobin());
    }

    private void checkParameterAnomaly(
            RegionStats stats,
            String parameter,
            Double avgValue,
            Double trend,
            Double stdDev
    ) {
        if (avgValue == null || trend == null) {
            return;
        }

        // Verificar condições para alerta coletivo
        boolean highAlertProportion = stats.getAlertProportion() >= alertProportionThreshold;
        boolean significantTrend = Math.abs(trend) >= trendIncreaseThreshold;

        if (highAlertProportion && significantTrend) {
            // Verificar se já existe alerta similar recente
            Integer recentAlertsCount = collectiveAlertRepository.countRecentAlerts(
                    stats.getRegion(),
                    parameter,
                    stats.getWindowStart().minusHours(24),
                    LocalDateTime.now()
            );

            if (recentAlertsCount == 0) {
                createCollectiveAlert(stats, parameter, avgValue, trend, stdDev);
            } else {
                log.debug("Alerta similar já existe para {} - {}",
                        stats.getRegion(), parameter);
            }
        }
    }

    private void createCollectiveAlert(
            RegionStats stats,
            String parameter,
            Double currentAvg,
            Double trend,
            Double stdDev
    ) {
        log.info("Criando alerta coletivo para região {} - parâmetro {}",
                stats.getRegion(), parameter);

        // Determinar severidade
        AlertSeverity severity = determineSeverity(
                stats.getAlertProportion(),
                Math.abs(trend)
        );

        // Gerar código único do alerta
        String alertCode = generateAlertCode(stats.getRegion(), parameter);

        // Calcular valor anterior
        Double previousAvg = trend != 0
                ? currentAvg / (1 + (trend / 100))
                : currentAvg;

        // Criar alerta
        CollectiveAlert alert = CollectiveAlert.builder()
                .alertCode(alertCode)
                .region(stats.getRegion())
                .parameter(parameter)
                .severity(severity)
                .affectedHemograms(stats.getAlertedHemograms())
                .totalHemograms(stats.getTotalHemograms())
                .alertProportion(stats.getAlertProportion())
                .currentAvg(currentAvg)
                .previousAvg(previousAvg)
                .trendPercent(trend)
                .stdDeviation(stdDev)
                .windowStart(stats.getWindowStart())
                .windowEnd(stats.getWindowEnd())
                .description(buildAlertDescription(stats, parameter, currentAvg, trend))
                .recommendedAction(buildRecommendedAction(parameter, severity))
                .createdAt(LocalDateTime.now())
                .acknowledged(false)
                .regionStats(stats)
                .build();

        alert = collectiveAlertRepository.save(alert);
        log.info("Alerta coletivo {} criado com sucesso", alertCode);

        // Enviar notificações
        notificationService.sendAlertNotifications(alert);
    }

    private AlertSeverity determineSeverity(Double alertProportion, Double trendMagnitude) {
        if (alertProportion >= 0.7 && trendMagnitude >= 50) {
            return AlertSeverity.CRITICAL;
        } else if (alertProportion >= 0.6 && trendMagnitude >= 35) {
            return AlertSeverity.HIGH;
        } else if (alertProportion >= 0.5 && trendMagnitude >= 25) {
            return AlertSeverity.MEDIUM;
        }
        return AlertSeverity.LOW;
    }

    private String generateAlertCode(String region, String parameter) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String paramCode = parameter.substring(0, 3).toUpperCase();
        String random = String.format("%03d", new Random().nextInt(1000));
        return String.format("ALERT-%s-%s-%s-%s", date, region, paramCode, random);
    }

    private String buildAlertDescription(
            RegionStats stats,
            String parameter,
            Double currentAvg,
            Double trend
    ) {
        String parameterName = switch (parameter) {
            case "leukocytes" -> "leucócitos";
            case "platelets" -> "plaquetas";
            case "hemoglobin" -> "hemoglobina";
            default -> parameter;
        };

        String direction = trend > 0 ? "aumento" : "redução";

        return String.format(
                "Detectado %s expressivo de %s na região %s. " +
                        "%.1f%% dos hemogramas apresentam valores alterados (%.1f%% %s em relação ao período anterior). " +
                        "Total de %d hemogramas analisados nas últimas %d horas.",
                direction,
                parameterName,
                stats.getRegion(),
                stats.getAlertProportion() * 100,
                Math.abs(trend),
                direction,
                stats.getTotalHemograms(),
                windowSizeHours
        );
    }

    private String buildRecommendedAction(String parameter, AlertSeverity severity) {
        String baseAction = switch (parameter) {
            case "leukocytes" -> "Investigar possível surto de infecção ou processo inflamatório coletivo.";
            case "platelets" -> "Avaliar possível exposição a agentes que causem plaquetopenia.";
            case "hemoglobin" -> "Investigar possível deficiência nutricional ou anemia coletiva.";
            default -> "Realizar investigação epidemiológica na região.";
        };

        String urgencyAction = severity == AlertSeverity.CRITICAL || severity == AlertSeverity.HIGH
                ? " AÇÃO IMEDIATA REQUERIDA: realizar busca ativa e notificação à vigilância epidemiológica."
                : " Monitorar evolução e considerar investigação in loco se persistir.";

        return baseAction + urgencyAction;
    }
}