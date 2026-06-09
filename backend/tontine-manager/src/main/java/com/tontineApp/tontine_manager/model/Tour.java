package com.tontineApp.tontine_manager.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="tour")
public class Tour {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_tour")
    private Integer id;
    @Column(name="ordre_tour")
    private Integer ordreTour;

    @ManyToOne
    private Membre beneficiaire;

    @ManyToOne
    private Cycle cycle;
}
