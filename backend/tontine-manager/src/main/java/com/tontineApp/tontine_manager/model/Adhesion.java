package com.tontineApp.tontine_manager.model;


import com.tontineApp.tontine_manager.enumeration.StatutAdhesion;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name="adhesion")
@Data
public class Adhesion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name="tontine_id")
    private Tontine tontine;

    @Enumerated(EnumType.STRING)
    private StatutAdhesion statut;
    private LocalDate dateAdhesion;
}
