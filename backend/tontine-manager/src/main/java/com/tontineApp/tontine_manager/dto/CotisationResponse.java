package com.tontineApp.tontine_manager.dto;

import com.tontineApp.tontine_manager.enumeration.StatutCotisation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CotisationResponse {
    private Integer id;
    private Integer montant;
    private StatutCotisation statutCotisation;
    private Integer idMembre;
    private String nomMembre;
    private String prenomMembre;
    private Integer idTontine;
    private String nomTontine;
}
