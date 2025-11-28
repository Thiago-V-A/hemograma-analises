package br.gov.saude.hemogram.controller;

import br.gov.saude.hemogram.controller.dto.DeviceRegistrationRequest;
import br.gov.saude.hemogram.service.notification.NotificationService;
import br.gov.saude.hemogram.service.notification.UserDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Gerenciamento de notificações push")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserDeviceService userDeviceService;

    @PostMapping("/register")
    @Operation(summary = "Registra dispositivo para receber notificações")
    public ResponseEntity<Void> registerDevice(
            @RequestBody DeviceRegistrationRequest request
    ) {
        userDeviceService.registerDevice(
                request.getUserId(),
                request.getDeviceToken(),
                request.getRegions()
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unregister/{token}")
    @Operation(summary = "Remove registro de dispositivo")
    public ResponseEntity<Void> unregisterDevice(@PathVariable String token) {
        userDeviceService.unregisterDevice(token);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Marca notificação como lida")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @RequestParam String userId
    ) {
        notificationService.markNotificationAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Conta notificações não lidas")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable String userId) {
        Integer count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
}