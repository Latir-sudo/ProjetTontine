package com.tontineApp.tontine_manager.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
@Entity
@Table(name="penalite")

public class Penalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_penalite")
    private Integer id;

    private Integer montantPenalite;
    private String motif;
    @Column(name="date_penalite")
    private LocalDate datePenalite;
    private String statutPenalite;

    @ManyToOne
    @JoinColumn (name="membre_id")
    private Membre membre;
}
