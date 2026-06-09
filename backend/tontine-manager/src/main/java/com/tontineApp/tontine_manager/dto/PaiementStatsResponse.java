// com/tontineApp/tontine_manager/dto/PaiementStatsResponse.java
package com.tontineApp.tontine_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaiementStatsResponse {
    private Long totalPaye;
    private Long totalAttendu;
    private Integer paiementsReussis;
    private Integer paiementsEnAttente;
    private Integer paiementsEchoues;
    private Double tauxCompletude;
}