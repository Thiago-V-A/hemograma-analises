package br.gov.saude.hemogram.repository;

import br.gov.saude.hemogram.model.AlertNotification;
import br.gov.saude.hemogram.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertNotificationRepository extends JpaRepository<AlertNotification, Long> {

    List<AlertNotification> findByAlertIdAndUserId(Long alertId, String userId);

    List<AlertNotification> findByUserIdAndIsRead(String userId, Boolean isRead);

    @Query("SELECT an FROM AlertNotification an WHERE an.status = :status " +
            "AND an.sentAt < :threshold")
    List<AlertNotification> findByStatusAndSentAtBefore(
            @Param("status") NotificationStatus status,
            @Param("threshold") LocalDateTime threshold
    );

    @Query("SELECT COUNT(an) FROM AlertNotification an WHERE an.userId = :userId " +
            "AND an.isRead = false")
    Integer countUnreadByUser(@Param("userId") String userId);

    List<AlertNotification> findByAlertId(Long alertId);

    @Query("SELECT an FROM AlertNotification an WHERE an.alert.id = :alertId " +
            "AND an.status = 'FAILED'")
    List<AlertNotification> findFailedNotificationsByAlert(@Param("alertId") Long alertId);
}