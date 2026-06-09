// com/tontineApp/tontine_manager/dto/PaiementResponse.java
package com.tontineApp.tontine_manager.dto;

import com.tontineApp.tontine_manager.enumeration.ModePaiement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaiementResponse {
    private Integer id;
    private Integer montant;
    private Date datePaiement;
    private ModePaiement modePaiement;
    private String reference;
    private Boolean valide;
    private Integer cotisationId;
    private String cotisationStatut;
}