package com.tontineApp.tontine_manager.dto;

import com.tontineApp.tontine_manager.enumeration.StatutAdhesion;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdhesionResponse {

    private Integer idUser;
    private Integer idTontine;

    @NotBlank
    private String prenomUser;
    private String nomUser;

    @NotBlank
    private String telephoneUser;

    private String emailUser;

    private LocalDate dateAdhesion;
    private StatutAdhesion statut;
}