package br.gov.saude.hemogram.controller.dto;

import lombok.Data;

@Data
public class AcknowledgeRequest {
    private String userId;
    private String notes;
}
