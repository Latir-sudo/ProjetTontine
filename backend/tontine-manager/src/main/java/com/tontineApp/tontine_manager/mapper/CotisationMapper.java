package com.tontineApp.tontine_manager.mapper;

import com.tontineApp.tontine_manager.dto.CotisationRequest;
import com.tontineApp.tontine_manager.dto.CotisationResponse;
import com.tontineApp.tontine_manager.model.Cotisation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CotisationMapper {

    public Cotisation toCotisation(CotisationRequest request) {
        if (request == null) {
            log.warn("CotisationRequest est null");
            return null;
        }

        Cotisation cotisation = new Cotisation();
        cotisation.setMontant(request.getMontant());
        // Le statut sera défini dans le service

        return cotisation;
    }


        public CotisationResponse toCotisationResponse(Cotisation cotisation) {
            if (cotisation == null) {
                log.warn("Cotisation est null");
                return null;
            }

            CotisationResponse response = new CotisationResponse();
            response.setId(cotisation.getId());
            response.setMontant(cotisation.getMontant());
            response.setStatutCotisation(cotisation.getStatut());

            // ✅ Vérifier que membre n'est pas null
            if (cotisation.getMembre() != null) {
                response.setIdMembre(cotisation.getMembre().getId());

                // ✅ Vérifier que user n'est pas null
                if (cotisation.getMembre().getUser() != null) {
                    response.setNomMembre(cotisation.getMembre().getUser().getNom());
                    response.setPrenomMembre(cotisation.getMembre().getUser().getPrenom());
                }
            }

            // ✅ Vérifier que tontine n'est pas null
            if (cotisation.getTontine() != null) {
                response.setIdTontine(cotisation.getTontine().getId());
                response.setNomTontine(cotisation.getTontine().getNomTontine());
            }

            return response;
        }
    }
