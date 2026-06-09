package com.tontineApp.tontine_manager.dto;
import com.tontineApp.tontine_manager.model.Role;
import jakarta.validation.constraints.*;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserRequest {

    private String prenom;
    private String nom;
    private String email;
    private String telephone;
    private String password;
    private LocalDate dateInscription;
    private String ville;
    List<String> roles;
}
