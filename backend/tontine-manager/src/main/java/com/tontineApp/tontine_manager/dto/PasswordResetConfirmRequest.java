package com.tontineApp.tontine_manager.dto;

import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    private String email;
    private String code;
    private String newPassword;
}
