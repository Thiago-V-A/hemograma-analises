package br.gov.saude.hemogram.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_notifications", indexes = {
        @Index(name = "idx_alert_user", columnList = "alert_id,user_id"),
        @Index(name = "idx_sent_at", columnList = "sent_at"),
        @Index(name = "idx_read", columnList = "is_read")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private CollectiveAlert alert;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "device_token", nullable = false, length = 500)
    private String deviceToken;

    @Column(name = "notification_title", length = 200)
    private String notificationTitle;

    @Column(name = "notification_body", columnDefinition = "TEXT")
    private String notificationBody;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "fcm_message_id", length = 200)
    private String fcmMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Version
    private Long version;
}