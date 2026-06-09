package com.tontineApp.tontine_manager.model;

import com.tontineApp.tontine_manager.enumeration.StatutCotisation;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name="cotisation")

public class Cotisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_cotisation")
    private Integer id;
    @Column(name="montant_total")
    private Integer montant;
    @Column(name="statut_cotisation")
    private StatutCotisation statut;

    @ManyToOne
    @JoinColumn(name="membre_id")
    private Membre membre;

    @ManyToOne
    @JoinColumn(name="tontine_id")
    private Tontine tontine;

}
