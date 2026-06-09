package com.tontineApp.tontine_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MembreRequest {

    private Integer idUser;
    private Integer idTontine;
    private LocalDate dateAdhesion;

}
