package com.tontineApp.tontine_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TontineResponse {
    private Integer id;
    private String nomTontine;
    private String frequence;
    private Integer montant;
    private Integer idAdmin;
    private String nomAdmin;
    private String PrenomAdmin;
    private String telephoneAdmin;
    private String descriptionTontine;
    private String categorie;
    private String region;
    private String statutTontine;
    private Integer nombreMembres;
    private Integer nombreMax;
}
