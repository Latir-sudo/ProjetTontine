package com.tontineApp.tontine_manager.dto;

import com.tontineApp.tontine_manager.enumeration.ModePaiement;
import lombok.Data;

@Data
public class MobilePaymentRequest {
    @jakarta.validation.constraints.NotNull
    private Integer cotisationId;

    // Optionnel : Wave limitera le paiement à ce numéro s'il est fourni.
    private String phoneNumber;
}
