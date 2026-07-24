package com.tontineApp.tontine_manager.model;

import com.tontineApp.tontine_manager.enumeration.StatutNotification;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Entity
@Table(name = "notification")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notification")
    private Integer id;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "heure_relative")
    private String heureRelative;

    @Column(name = "couleur")
    private String couleur;

    @Column(name = "est_lu")
    @Builder.Default
    private Boolean estLu = false;

    @Column(name = "date_creation")
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "membre_id")
    private Membre membre;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "statut_notification")
    @Enumerated(EnumType.STRING)
    private StatutNotification statutNotification;

    @Column(name = "lien_action")
    private String lienAction;

    @Column(name = "type_notification")
    private String typeNotification; // PAIEMENT, RAPPEL, MEMBRE, RETARD, etc.

    // Méthode utilitaire pour formater la date
    public String getDateFormatee() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateCreation.format(formatter);
    }

    // Méthode pour obtenir le temps relatif
    public String getTempsRelatif() {
        if (heureRelative != null && !heureRelative.isEmpty()) {
            return heureRelative;
        }


        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(dateCreation, now);

        long heures = duration.toHours();
        long jours = duration.toDays();

        if (heures < 1) {
            return "à l'instant";
        } else if (heures < 24) {
            return heures + "h";
        } else {
            return jours + "j";
        }
    }

    // Marquer comme lu
    public void marquerCommeLu() {
        this.estLu = true;
        this.statutNotification = StatutNotification.LU;
    }

    // Marquer comme non lu
    public void marquerCommeNonLu() {
        this.estLu = false;
        this.statutNotification = StatutNotification.NON_LU;
    }

    // Vérifier si la notification est récente (moins de 24h)
    public boolean estRecente() {
        LocalDate now = LocalDate.now();
        java.time.Duration duration = java.time.Duration.between(dateCreation, now);
        return duration.toHours() < 24;
    }
}