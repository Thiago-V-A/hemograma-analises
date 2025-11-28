package br.gov.saude.hemogram.repository;

import br.gov.saude.hemogram.model.Hemogram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HemogramRepository extends JpaRepository<Hemogram, Long> {

    Optional<Hemogram> findByFhirId(String fhirId);

    List<Hemogram> findByPatientRegionAndReceivedAtBetween(
            String region,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("SELECT COUNT(h) FROM Hemogram h WHERE h.patientRegion = :region " +
            "AND h.receivedAt BETWEEN :start AND :end")
    Integer countByRegionAndWindow(
            @Param("region") String region,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(h) FROM Hemogram h WHERE h.patientRegion = :region " +
            "AND h.receivedAt BETWEEN :start AND :end " +
            "AND h.hasIndividualAlert = true")
    Integer countAlertedByRegionAndWindow(
            @Param("region") String region,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT AVG(h.leukocytes) FROM Hemogram h WHERE h.patientRegion = :region " +
            "AND h.receivedAt BETWEEN :start AND :end AND h.leukocytes IS NOT NULL")
    Double calculateAvgLeukocytes(
            @Param("region") String region,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT AVG(h.platelets) FROM Hemogram h WHERE h.patientRegion = :region " +
            "AND h.receivedAt BETWEEN :start AND :end AND h.platelets IS NOT NULL")
    Double calculateAvgPlatelets(
            @Param("region") String region,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT AVG(h.hemoglobin) FROM Hemogram h WHERE h.patientRegion = :region " +
            "AND h.receivedAt BETWEEN :start AND :end AND h.hemoglobin IS NOT NULL")
    Double calculateAvgHemoglobin(
            @Param("region") String region,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT DISTINCT h.patientRegion FROM Hemogram h " +
            "WHERE h.receivedAt BETWEEN :start AND :end")
    List<String> findActiveRegions(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT h FROM Hemogram h WHERE h.patientRegion = :region " +
            "AND h.receivedAt BETWEEN :start AND :end " +
            "AND h.hasIndividualAlert = true")
    List<Hemogram> findAlertedHemogramsByRegionAndWindow(
            @Param("region") String region,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}