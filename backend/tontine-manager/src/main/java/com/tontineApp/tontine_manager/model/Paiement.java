package com.tontineApp.tontine_manager.model;


import com.tontineApp.tontine_manager.enumeration.ModePaiement;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name="paiement")
public class Paiement {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id_paiement")
    private Integer id;
    private Integer montant;
    @Column(name="date_paiement")
    private Date datePaiement;
    @Column(name="mode_paiement")

    @Enumerated(EnumType.STRING)
    private ModePaiement modePaiement;
    private String reference;
    private Boolean valide;

    @ManyToOne
    @JoinColumn(name="cotisation_id")
    private Cotisation cotisation;
}
