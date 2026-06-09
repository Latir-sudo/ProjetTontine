// com/tontineApp/tontine_manager/mapper/PaiementMapper.java
package com.tontineApp.tontine_manager.mapper;

import com.tontineApp.tontine_manager.dto.PaiementHistoriqueResponse;
import com.tontineApp.tontine_manager.dto.PaiementResponse;
import com.tontineApp.tontine_manager.model.Paiement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaiementMapper {

    /**
     * Convertit un Paiement en PaiementResponse
     */
    public PaiementResponse toResponse(Paiement paiement) {
        if (paiement == null) {
            return null;
        }

        PaiementResponse response = new PaiementResponse();
        response.setId(paiement.getId());
        response.setMontant(paiement.getMontant());
        response.setDatePaiement(paiement.getDatePaiement());
        response.setModePaiement(paiement.getModePaiement());
        response.setReference(paiement.getReference());
        response.setValide(paiement.getValide());

        // Récupérer les infos de la cotisation
        if (paiement.getCotisation() != null) {
            response.setCotisationId(paiement.getCotisation().getId());
            if (paiement.getCotisation().getStatut() != null) {
                response.setCotisationStatut(paiement.getCotisation().getStatut().name());
            }
        }

        return response;
    }

    /**
     * Convertit un Paiement en PaiementHistoriqueResponse
     */
    public PaiementHistoriqueResponse toHistoriqueResponse(Paiement paiement) {
        if (paiement == null) {
            return null;
        }

        PaiementHistoriqueResponse response = new PaiementHistoriqueResponse();
        response.setId(paiement.getId());
        response.setMontant(paiement.getMontant());
        response.setDatePaiement(paiement.getDatePaiement());
        response.setModePaiement(paiement.getModePaiement());
        response.setReference(paiement.getReference());
        response.setValide(paiement.getValide());

        // Récupérer les infos de la cotisation et du membre
        if (paiement.getCotisation() != null) {
            response.setCotisationId(paiement.getCotisation().getId());

            if (paiement.getCotisation().getMembre() != null) {
                // Récupérer le nom et prénom depuis l'utilisateur (Users)
                if (paiement.getCotisation().getMembre().getUser() != null) {
                    response.setMembreNom(paiement.getCotisation().getMembre().getUser().getNom());
                    response.setMembrePrenom(paiement.getCotisation().getMembre().getUser().getPrenom());

                    // Générer le titre
                    String titre = String.format("Cotisation - %s %s",
                            response.getMembrePrenom(),
                            response.getMembreNom());
                    response.setTitreCotisation(titre);
                }
            }
        }

        return response;
    }

    /**
     * Convertit une liste de Paiements en liste de PaiementResponse
     */
    public List<PaiementResponse> toResponseList(List<Paiement> paiements) {
        if (paiements == null) {
            return null;
        }
        return paiements.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une liste de Paiements en liste de PaiementHistoriqueResponse
     */
    public List<PaiementHistoriqueResponse> toHistoriqueResponseList(List<Paiement> paiements) {
        if (paiements == null) {
            return null;
        }
        return paiements.stream()
                .map(this::toHistoriqueResponse)
                .collect(Collectors.toList());
    }
}