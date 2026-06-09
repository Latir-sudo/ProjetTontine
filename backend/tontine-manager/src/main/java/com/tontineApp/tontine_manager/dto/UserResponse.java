package com.tontineApp.tontine_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Integer id;
    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private LocalDate dateInscription;
    private String ville;
    private List<String> roles;


}
