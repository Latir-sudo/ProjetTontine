package com.tontineApp.tontine_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTokenDto {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String ville;
    private LocalDate dateInscription;
    private List<String> roles;  // ← Liste de strings, pas d'entités
}