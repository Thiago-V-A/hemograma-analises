package br.gov.saude.hemogram.repository;

import br.gov.saude.hemogram.model.RegionStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegionStatsRepository extends JpaRepository<RegionStats, Long> {

    Optional<RegionStats> findByRegionAndWindowStartAndWindowEnd(
            String region,
            LocalDateTime windowStart,
            LocalDateTime windowEnd
    );

    @Query("SELECT rs FROM RegionStats rs WHERE rs.region = :region " +
            "AND rs.windowEnd < :currentWindowStart " +
            "ORDER BY rs.windowEnd DESC")
    List<RegionStats> findPreviousStats(
            @Param("region") String region,
            @Param("currentWindowStart") LocalDateTime currentWindowStart
    );

    Optional<RegionStats> findFirstByRegionAndWindowEndBeforeOrderByWindowEndDesc(
            String region,
            LocalDateTime windowEnd
    );

    List<RegionStats> findByRegionAndWindowEndBetween(
            String region,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT DISTINCT rs.region FROM RegionStats rs " +
            "WHERE rs.windowEnd BETWEEN :start AND :end")
    List<String> findRegionsWithStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT rs FROM RegionStats rs WHERE rs.windowEnd >= :threshold")
    List<RegionStats> findRecentStats(@Param("threshold") LocalDateTime threshold);
}