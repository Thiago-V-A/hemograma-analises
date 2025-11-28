package br.gov.saude.hemogram.controller.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeviceRegistrationRequest {
    private String userId;
    private String deviceToken;
    private List<String> regions;
}