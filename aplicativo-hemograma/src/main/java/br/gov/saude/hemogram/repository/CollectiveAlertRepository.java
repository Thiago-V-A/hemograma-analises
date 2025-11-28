package br.gov.saude.hemogram.repository;

import br.gov.saude.hemogram.model.AlertSeverity;
import br.gov.saude.hemogram.model.CollectiveAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectiveAlertRepository extends JpaRepository<CollectiveAlert, Long> {

    Optional<CollectiveAlert> findByAlertCode(String alertCode);

    Page<CollectiveAlert> findByRegionAndCreatedAtBetween(
            String region,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<CollectiveAlert> findBySeverityAndAcknowledged(
            AlertSeverity severity,
            Boolean acknowledged,
            Pageable pageable
    );

    @Query("SELECT ca FROM CollectiveAlert ca WHERE ca.acknowledged = false " +
            "ORDER BY ca.severity DESC, ca.createdAt DESC")
    List<CollectiveAlert> findUnacknowledgedAlerts();

    @Query("SELECT ca FROM CollectiveAlert ca WHERE " +
            "(:region IS NULL OR ca.region = :region) AND " +
            "(:severity IS NULL OR ca.severity = :severity) AND " +
            "(:acknowledged IS NULL OR ca.acknowledged = :acknowledged) AND " +
            "ca.createdAt BETWEEN :start AND :end " +
            "ORDER BY ca.createdAt DESC")
    Page<CollectiveAlert> findByFilters(
            @Param("region") String region,
            @Param("severity") AlertSeverity severity,
            @Param("acknowledged") Boolean acknowledged,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    @Query("SELECT COUNT(ca) FROM CollectiveAlert ca WHERE ca.region = :region " +
            "AND ca.parameter = :parameter " +
            "AND ca.createdAt BETWEEN :start AND :end")
    Integer countRecentAlerts(
            @Param("region") String region,
            @Param("parameter") String parameter,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<CollectiveAlert> findTop10ByOrderByCreatedAtDesc();
}