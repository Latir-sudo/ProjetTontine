package com.tontineApp.tontine_manager.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name="tontine")
public class Tontine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_tontine")
    private Integer id;

    @Column(name="nom_tontine")
    private String nomTontine;

    @Column(name="decription_tontine")
    private String descriptionTontine;

    @Column(name="montant_cotisation")
    private Integer montant;
    private String frequence;
    @Column(name="date_de_creation")
    private LocalDate dateCreation;

    @Column(name="statut_tontine")
    private String statutTontine;
    @Column(name="politique_tontine")
    private String politiqueTontine;

    @Column(name="code_acces")
    private String codeAcces;

    private String regionTontine;
    private String categorieTontine;
    private Integer nombreMembres;
    private Integer nombreMax;

    @ManyToOne
    private Users admin;

}
