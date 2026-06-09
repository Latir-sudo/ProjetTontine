package com.tontineApp.tontine_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembreResponse {
    private Integer id;
    private Integer idUser;
    private Integer idTontine;
    private String prenom;
    private String nom;
    private String telephone;
    private String email;
    private LocalDate dateAdhesion;
}
