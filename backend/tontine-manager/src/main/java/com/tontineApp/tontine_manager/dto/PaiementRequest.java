// com/tontineApp/tontine_manager/dto/PaiementRequest.java
package com.tontineApp.tontine_manager.dto;

import com.tontineApp.tontine_manager.enumeration.ModePaiement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaiementRequest {
    private Integer montant;
    private Date datePaiement;
    private ModePaiement modePaiement;
    private String reference;
}