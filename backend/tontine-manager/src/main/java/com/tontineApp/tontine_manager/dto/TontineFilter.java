package com.tontineApp.tontine_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TontineFilter {

    private String categorie;
    private String region;
    private String frequence;
    private Integer montant;
}
