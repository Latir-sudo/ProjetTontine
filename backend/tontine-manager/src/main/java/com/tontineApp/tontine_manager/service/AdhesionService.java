package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.dto.AdhesionRequest;
import com.tontineApp.tontine_manager.dto.AdhesionResponse;
import com.tontineApp.tontine_manager.dto.MembreRequest;
import com.tontineApp.tontine_manager.dto.UpdateStatusDto;
import com.tontineApp.tontine_manager.enumeration.StatutAdhesion;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.exception.UnAuthorizedException;
import com.tontineApp.tontine_manager.mapper.AdhesionMapper;
import com.tontineApp.tontine_manager.model.Adhesion;
import com.tontineApp.tontine_manager.model.Tontine;
import com.tontineApp.tontine_manager.repository.AdhesionRepository;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.tontineApp.tontine_manager.enumeration.StatutAdhesion.ACCEPTEE;

@Slf4j
@Service
@AllArgsConstructor
public class AdhesionService {

    private final AdhesionRepository adhesionRepository;
    private final TontineRepository tontineRepository;
    private final AdhesionMapper adhesionMapper;
    private final MembreService membreService;

    public List<AdhesionResponse> getAdhesionAttente(Integer idTontine) {
        log.info("Recuperation des adhesions en attente pour la tontine {}", idTontine);

        if (!tontineRepository.existsById(idTontine)) {
            throw new RessourceNotFoundException("Tontine " + idTontine + " non trouvee");
        }

        List<Adhesion> adhesions = adhesionRepository.findByStatutAndTontine_Id(StatutAdhesion.ATTENTE, idTontine);
        log.info("{} adhesion(s) en attente trouvee(s)", adhesions.size());

        return adhesions.stream()
                .map(adhesionMapper::toAdhesionResponse)
                .toList();
    }

    @Transactional
    public AdhesionResponse traiterAdhesion(Integer idUser, UpdateStatusDto nouveau, Integer idTontine) {
        if (nouveau == null || nouveau.getStatut() == null) {
            throw new IllegalArgumentException("Le statut ne peut pas etre null");
        }

        StatutAdhesion nouveauStatut = StatutAdhesion.from(nouveau.getStatut());
        log.info("Traitement de l'adhesion pour user={}, tontine={}, nouveau statut={}", idUser, idTontine, nouveauStatut);

        Adhesion adhesion = adhesionRepository.findByUser_IdAndTontine_Id(idUser, idTontine)
                .orElseThrow(() -> new RessourceNotFoundException(
                        String.format("Adhesion non trouvee pour l'utilisateur %d dans la tontine %d", idUser, idTontine)
                ));

        if (ACCEPTEE.equals(nouveauStatut) && adhesionRepository.existsByUser_IdAndTontine_IdAndStatut(idUser, idTontine, ACCEPTEE)) {
            throw new UnAuthorizedException("utilisateur deja membre");
        }

        adhesion.setStatut(nouveauStatut);
        if (ACCEPTEE.equals(nouveauStatut) && adhesion.getDateAdhesion() == null) {
            adhesion.setDateAdhesion(LocalDate.now());
        }

        if (ACCEPTEE.equals(nouveauStatut)) {
            MembreRequest membreRequest = new MembreRequest();
            membreRequest.setIdTontine(idTontine);
            membreRequest.setIdUser(idUser);
            membreService.ajouterUtilisateurATontine(membreRequest);
        }

        Adhesion saved = adhesionRepository.save(adhesion);
        log.info("Adhesion traitee avec succes, nouveau statut: {}", saved.getStatut());

        return adhesionMapper.toAdhesionResponse(saved);
    }

    public AdhesionResponse save(AdhesionRequest request, Integer idTontine) {
        log.info("Creation d'une demande d'adhesion pour user={}, tontine={}", request.getIdUser(), idTontine);

        if (request == null) {
            throw new IllegalArgumentException("La requete d'adhesion ne peut pas etre null");
        }

        Tontine tontine = tontineRepository.findById(idTontine)
                .orElseThrow(() -> new RessourceNotFoundException("Tontine " + idTontine + " non trouvee"));

        Adhesion adhesion = adhesionMapper.toAdhesion(request);
        adhesion.setTontine(tontine);
        Adhesion saved = adhesionRepository.save(adhesion);
        log.info("Demande d'adhesion creee avec succes, id={}", saved.getId());

        return adhesionMapper.toAdhesionResponse(saved);
    }

    @Transactional
    public AdhesionResponse ajouterUser(AdhesionRequest request, Integer idTontine) {
        if (request == null) {
            throw new IllegalArgumentException("La requete d'adhesion ne peut pas etre null");
        }

        Tontine tontine = tontineRepository.findById(idTontine)
                .orElseThrow(() -> new RessourceNotFoundException("Tontine " + idTontine + " non trouvee"));

        Adhesion adhesion = adhesionMapper.toAdhesion(request);
        adhesion.setTontine(tontine);
        Adhesion saved = adhesionRepository.save(adhesion);

        UpdateStatusDto statusDto = new UpdateStatusDto(ACCEPTEE.name());
        this.traiterAdhesion(request.getIdUser(), statusDto, idTontine);

        log.info("Demande d'adhesion creee avec succes, id={}", saved.getId());

        return adhesionMapper.toAdhesionResponse(saved);
    }
}
