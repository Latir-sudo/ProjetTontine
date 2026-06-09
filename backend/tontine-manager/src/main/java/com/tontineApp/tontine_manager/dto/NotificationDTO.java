package com.tontineApp.tontine_manager.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Integer id;
    private String titre;
    private String message;
    private String tempsRelatif;
    private String couleur;
    private Boolean estLu;
    private String dateCreation;
    private String typeNotification;
    private String lienAction;
}