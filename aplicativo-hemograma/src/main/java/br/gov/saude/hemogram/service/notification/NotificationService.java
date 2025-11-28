// ============ NotificationService.java ============
package br.gov.saude.hemogram.service.notification;

import br.gov.saude.hemogram.model.*;
import br.gov.saude.hemogram.repository.AlertNotificationRepository;
import br.gov.saude.hemogram.service.fhir.FhirCommunicationService;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final AlertNotificationRepository notificationRepository;
    private final FhirCommunicationService fhirCommunicationService;
    private final UserDeviceService userDeviceService;

    @Value("${notification.enabled}")
    private Boolean notificationEnabled;

    @Value("${firebase.enabled}")
    private Boolean firebaseEnabled;

    @Async
    @Transactional
    public void sendAlertNotifications(CollectiveAlert alert) {
        if (!notificationEnabled) {
            log.debug("Notificações desabilitadas, pulando envio");
            return;
        }

        log.info("Enviando notificações para alerta {}", alert.getAlertCode());

        // Criar Communication FHIR
        String communicationId = fhirCommunicationService.createCommunicationForAlert(alert);
        if (communicationId != null) {
            alert.setFhirCommunicationId(communicationId);
        }

        // Obter gestores da região
        List<UserDevice> devices = userDeviceService.getDevicesForRegion(alert.getRegion());

        if (devices.isEmpty()) {
            log.warn("Nenhum dispositivo encontrado para região {}", alert.getRegion());
            return;
        }

        log.info("Enviando notificações para {} dispositivos", devices.size());

        // Preparar mensagem
        String title = buildNotificationTitle(alert);
        String body = buildNotificationBody(alert);

        // Enviar para cada dispositivo
        for (UserDevice device : devices) {
            sendToDevice(alert, device, title, body);
        }
    }

    private void sendToDevice(
            CollectiveAlert alert,
            UserDevice device,
            String title,
            String body
    ) {
        AlertNotification notification = AlertNotification.builder()
                .alert(alert)
                .userId(device.getUserId())
                .deviceToken(device.getToken())
                .notificationTitle(title)
                .notificationBody(body)
                .sentAt(LocalDateTime.now())
                .isRead(false)
                .status(NotificationStatus.PENDING)
                .build();

        try {
            if (firebaseEnabled) {
                String messageId = sendFirebaseMessage(device.getToken(), title, body, alert);
                notification.setFcmMessageId(messageId);
                notification.setStatus(NotificationStatus.SENT);
                log.info("Notificação enviada para usuário {}: {}", device.getUserId(), messageId);
            } else {
                log.info("Firebase desabilitado, notificação simulada para {}", device.getUserId());
                notification.setStatus(NotificationStatus.SENT);
            }
        } catch (Exception e) {
            log.error("Erro ao enviar notificação para {}", device.getUserId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
        }

        notificationRepository.save(notification);
    }

    private String sendFirebaseMessage(
            String token,
            String title,
            String body,
            CollectiveAlert alert
    ) throws FirebaseMessagingException {

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("alert_id", alert.getId().toString())
                .putData("alert_code", alert.getAlertCode())
                .putData("region", alert.getRegion())
                .putData("severity", alert.getSeverity().name())
                .putData("parameter", alert.getParameter())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(getAndroidPriority(alert.getSeverity()))
                        .setNotification(AndroidNotification.builder()
                                .setColor("#FF0000")
                                .setSound("alert")
                                .setChannelId("collective_alerts")
                                .build())
                        .build())
                .build();

        return FirebaseMessaging.getInstance().send(message);
    }

    private AndroidConfig.Priority getAndroidPriority(AlertSeverity severity) {
        return (severity == AlertSeverity.CRITICAL || severity == AlertSeverity.HIGH)
                ? AndroidConfig.Priority.HIGH
                : AndroidConfig.Priority.NORMAL;
    }

    private String buildNotificationTitle(CollectiveAlert alert) {
        String severityPrefix = switch(alert.getSeverity()) {
            case CRITICAL -> "🔴 CRÍTICO";
            case HIGH -> "🟠 ALTA";
            case MEDIUM -> "🟡 MÉDIA";
            case LOW -> "🟢 BAIXA";
        };

        return String.format("%s - Alerta %s", severityPrefix, alert.getRegion());
    }

    private String buildNotificationBody(CollectiveAlert alert) {
        String parameterName = switch(alert.getParameter()) {
            case "leukocytes" -> "leucócitos";
            case "platelets" -> "plaquetas";
            case "hemoglobin" -> "hemoglobina";
            default -> alert.getParameter();
        };

        return String.format(
                "Detectado padrão anômalo em %s. %d de %d hemogramas afetados (%.0f%%). Toque para detalhes.",
                parameterName,
                alert.getAffectedHemograms(),
                alert.getTotalHemograms(),
                alert.getAlertProportion() * 100
        );
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId, String userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUserId().equals(userId)) {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                notificationRepository.save(notification);
                log.info("Notificação {} marcada como lida por {}", notificationId, userId);
            }
        });
    }

    public List<AlertNotification> getUserNotifications(String userId, Boolean unreadOnly) {
        if (Boolean.TRUE.equals(unreadOnly)) {
            return notificationRepository.findByUserIdAndIsRead(userId, false);
        }
        return notificationRepository.findByUserIdAndIsRead(userId, null);
    }

    public Integer getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUser(userId);
    }
}

