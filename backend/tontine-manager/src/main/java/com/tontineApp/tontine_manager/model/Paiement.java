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
    @Column(name = "provider_checkout_id", unique = true)
    private String providerCheckoutId;
    @Column(name = "provider_transaction_id")
    private String providerTransactionId;
    @Column(name = "provider_status")
    private String providerStatus;

    @ManyToOne
    @JoinColumn(name="cotisation_id")
    private Cotisation cotisation;
}
