package com.tontineApp.tontine_manager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name="cycle")
public class Cycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_cycle")
    private Integer id;
    @Column(name="date_debut")
    private Date dateDebut;
    @Column(name="date_fin")
    private Date dateFin;
    @Column(name="statut_cycle")
    private String statutCycle;


    @ManyToOne
    @JoinColumn(name="tontine_id")
    private Tontine tontine;



}
