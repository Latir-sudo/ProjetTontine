package com.tontineApp.tontine_manager.dto;

import com.tontineApp.tontine_manager.enumeration.StatutNotification;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NotificationResponse {
    private Integer id;
    private String titre;
    private String message;
    private String tempsRelatif;
    private String couleur;
    private Boolean estLu;
    private Integer idTontine;
    private String nomTontine;
    private Integer idMembre;
    private String prenomMembre;
    private String nomMembre;
    private LocalDate dateCreation;
    private StatutNotification statutNotification;
    private String typeNotification;
    private String lienAction;
}
