package com.tontineApp.tontine_manager.mapper;

import com.tontineApp.tontine_manager.dto.AdhesionRequest;
import com.tontineApp.tontine_manager.dto.AdhesionResponse;
import com.tontineApp.tontine_manager.dto.UpdateStatusDto;
import com.tontineApp.tontine_manager.enumeration.StatutAdhesion;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.model.Adhesion;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import static com.tontineApp.tontine_manager.enumeration.StatutAdhesion.ATTENTE;

@Component
@AllArgsConstructor
public class AdhesionMapper {

    private final UserRepository userRepository;
    private final TontineRepository tontineRepository;

    public AdhesionResponse toAdhesionResponse(Adhesion adhesion) {
        if (adhesion == null) {
            throw new IllegalArgumentException("L'adhésion ne doit pas être null");
        }

        AdhesionResponse response = new AdhesionResponse();

        if (adhesion.getUser() != null) {
            response.setPrenomUser(adhesion.getUser().getPrenom());
            response.setNomUser(adhesion.getUser().getNom());
            response.setTelephoneUser(adhesion.getUser().getTelephone());
            response.setEmailUser(adhesion.getUser().getEmail());
        } else {
            response.setPrenomUser("Inconnu");
            response.setNomUser("Inconnu");
            response.setTelephoneUser("Non renseigné");
        }

        response.setDateAdhesion(adhesion.getDateAdhesion());
        response.setStatut(adhesion.getStatut());
        response.setIdUser(adhesion.getUser() != null ? adhesion.getUser().getId() : null);
        response.setIdTontine(adhesion.getTontine() != null ? adhesion.getTontine().getId() : null);

        return response;
    }

    public Adhesion toAdhesion(AdhesionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La requête d'adhésion ne doit pas être null");
        }

        Adhesion adhesion = new Adhesion();

        // Récupérer l'utilisateur
        Users user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvé"));
        adhesion.setUser(user);

        adhesion.setDateAdhesion(request.getDateAdhesion());
        adhesion.setStatut(ATTENTE);

        return adhesion;
    }

    public void updateAdhesionFromDto(Adhesion adhesion, UpdateStatusDto dto) {
        if (dto.getStatut() != null) {
            adhesion.setStatut(StatutAdhesion.from(dto.getStatut()));
        }
    }
}
