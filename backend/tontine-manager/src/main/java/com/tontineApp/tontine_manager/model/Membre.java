package com.tontineApp.tontine_manager.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;


@Data
@Entity
@Table(name="membre")

public class Membre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private Users user;
    @ManyToOne
    @JoinColumn(name="tontine_id")
    private Tontine tontine;

    private LocalDate dateAdhesion;

    @Column(name="preference_notification")
    private String preferenceNotification;


}
