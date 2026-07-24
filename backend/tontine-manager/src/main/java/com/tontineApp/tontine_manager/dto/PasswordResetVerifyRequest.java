package com.tontineApp.tontine_manager.dto;

import lombok.Data;

@Data
public class PasswordResetVerifyRequest {
    private String email;
    private String code;
}
