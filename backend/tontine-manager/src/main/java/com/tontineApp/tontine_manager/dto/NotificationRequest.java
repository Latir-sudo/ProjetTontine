package com.tontineApp.tontine_manager.dto;

import com.tontineApp.tontine_manager.enumeration.StatutNotification;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class NotificationRequest {
    private String titre;
    private String message;
    private String tempsRelatif;
    private String couleur;
    private Boolean estLu;
    private Integer idTontine;
    private Integer idUser;
    private LocalDateTime dateCreation;
    private StatutNotification statutNotification;
    private String typeNotification;
    private String lienAction;
}
