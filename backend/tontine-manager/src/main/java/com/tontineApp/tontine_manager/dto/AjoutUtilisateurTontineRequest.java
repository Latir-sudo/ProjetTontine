package com.tontineApp.tontine_manager.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AjoutUtilisateurTontineRequest {
    private Integer tontineId;
    private Integer IdUser;

}
