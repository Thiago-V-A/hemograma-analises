package br.gov.saude.hemogram.service.notification;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserDeviceService {

    // Em produção, isso seria armazenado em banco de dados
    private final Map<String, List<UserDevice>> userDevicesByRegion = new ConcurrentHashMap<>();
    private final Map<String, UserDevice> devicesByToken = new ConcurrentHashMap<>();

    public void registerDevice(String userId, String token, List<String> regions) {
        log.info("Registrando dispositivo para usuário {} em regiões {}", userId, regions);

        UserDevice device = new UserDevice(userId, token, regions);
        devicesByToken.put(token, device);

        // Adicionar às regiões
        for (String region : regions) {
            userDevicesByRegion.computeIfAbsent(region, k -> new ArrayList<>())
                    .add(device);
        }
    }

    public void unregisterDevice(String token) {
        UserDevice device = devicesByToken.remove(token);
        if (device != null) {
            log.info("Removendo dispositivo de usuário {}", device.getUserId());

            // Remover de todas as regiões
            for (String region : device.getRegions()) {
                List<UserDevice> devices = userDevicesByRegion.get(region);
                if (devices != null) {
                    devices.removeIf(d -> d.getToken().equals(token));
                }
            }
        }
    }

    public List<UserDevice> getDevicesForRegion(String region) {
        return userDevicesByRegion.getOrDefault(region, Collections.emptyList())
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public void updateRegions(String token, List<String> newRegions) {
        UserDevice device = devicesByToken.get(token);
        if (device != null) {
            // Remover de regiões antigas
            for (String oldRegion : device.getRegions()) {
                List<UserDevice> devices = userDevicesByRegion.get(oldRegion);
                if (devices != null) {
                    devices.remove(device);
                }
            }

            // Atualizar regiões
            device.setRegions(newRegions);

            // Adicionar às novas regiões
            for (String newRegion : newRegions) {
                userDevicesByRegion.computeIfAbsent(newRegion, k -> new ArrayList<>())
                        .add(device);
            }

            log.info("Regiões atualizadas para dispositivo {}: {}", token, newRegions);
        }
    }

    @Data
    public static class UserDevice {
        private final String userId;
        private final String token;
        private List<String> regions;

        public UserDevice(String userId, String token, List<String> regions) {
            this.userId = userId;
            this.token = token;
            this.regions = new ArrayList<>(regions);
        }
    }
}