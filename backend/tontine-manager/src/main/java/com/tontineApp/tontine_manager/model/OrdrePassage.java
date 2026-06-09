package com.tontineApp.tontine_manager.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="ordre_passage")
public class OrdrePassage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_ordre")
    private Integer id;

    private Integer position;

    @ManyToOne
    @JoinColumn(name="id_cycle")
    private Cycle cycle;

    @ManyToOne
    @JoinColumn(name="membre_id")
    private Membre membre;
}
