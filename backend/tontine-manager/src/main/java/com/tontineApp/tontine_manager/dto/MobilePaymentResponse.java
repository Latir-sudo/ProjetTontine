package com.tontineApp.tontine_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MobilePaymentResponse {
    private boolean success;
    private String transactionId;
    private String provider;
    private String message;
    private String phoneNumber;
    private Integer amount;
    private String reference;
    private String status;
    private String launchUrl;
}
