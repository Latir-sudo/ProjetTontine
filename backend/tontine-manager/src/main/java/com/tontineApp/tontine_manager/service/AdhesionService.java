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
import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.model.Tontine;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.AdhesionRepository;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.tontineApp.tontine_manager.enumeration.StatutAdhesion.ACCEPTEE;

@Slf4j
@Service
@AllArgsConstructor
public class AdhesionService {

    private final AdhesionRepository adhesionRepository;
    private final TontineRepository tontineRepository;
    private final AdhesionMapper adhesionMapper;
    private final MembreService membreService;
    private final MembreRepository membreRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

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

        Adhesion adhesion = adhesionRepository
                .findByUser_IdAndTontine_IdAndStatut(idUser, idTontine, StatutAdhesion.ATTENTE)
                .orElseThrow(() -> new RessourceNotFoundException(
                        String.format("Adhesion en attente non trouvee pour l'utilisateur %d dans la tontine %d", idUser, idTontine)
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

            // Notifier le nouveau membre : sa demande a été acceptée
            envoyerNotificationAcceptation(idUser, idTontine, adhesion.getTontine());
        }

        if (StatutAdhesion.REJETEE.equals(nouveauStatut)) {
            log.info("Demande rejetee pour user={}, tontine={}", idUser, idTontine);
            envoyerNotificationRejet(idUser, adhesion.getTontine());
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

        // Notifier l'admin de la tontine qu'une nouvelle demande est arrivée
        envoyerNotificationAdmin(tontine, adhesion);

        // Notifier le demandeur que sa demande a été envoyée
        envoyerNotificationDemandeur(adhesion.getUser(), tontine);

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

    // ==================== MÉTHODES PRIVÉES NOTIFICATIONS ====================

    /**
     * Notifie l'admin de la tontine qu'une nouvelle demande d'adhésion a été soumise.
     * L'admin est forcément membre (il a créé la tontine).
     */
    private void envoyerNotificationAdmin(Tontine tontine, Adhesion adhesion) {
        if (tontine.getAdmin() == null) return;

        Optional<Membre> membreAdmin = membreRepository
                .findByTontine_IdAndUser_Id(tontine.getId(), tontine.getAdmin().getId());

        if (membreAdmin.isEmpty()) {
            log.warn("Admin de la tontine {} introuvable dans la table membre — notification non envoyée", tontine.getId());
            return;
        }

        String prenomDemandeur = adhesion.getUser() != null ? adhesion.getUser().getPrenom() : "Un utilisateur";
        String nomDemandeur    = adhesion.getUser() != null ? adhesion.getUser().getNom()    : "";

        notificationService.creerNotificationDirecte(
                membreAdmin.get(),
                "Nouvelle demande d'adhésion",
                prenomDemandeur + " " + nomDemandeur + " souhaite rejoindre la tontine « " + tontine.getNomTontine() + " ».",
                "ADHESION",
                "#0052cc",
                "/tontine/" + tontine.getId() + "?showDemandes=true"
        );
    }

    /**
     * Notifie le nouveau membre que sa demande d'adhésion a été acceptée.
     * Appelé après que membreService.ajouterUtilisateurATontine() a créé le Membre.
     */
    private void envoyerNotificationAcceptation(Integer idUser, Integer idTontine, Tontine tontine) {
        Optional<Membre> membreOpt = membreRepository.findByTontine_IdAndUser_Id(idTontine, idUser);

        if (membreOpt.isEmpty()) {
            log.warn("Membre introuvable après acceptation (user={}, tontine={}) — notification non envoyée", idUser, idTontine);
            return;
        }

        String nomTontine = tontine != null ? tontine.getNomTontine() : "la tontine";

        notificationService.creerNotificationDirecte(
                membreOpt.get(),
                "Demande d'adhésion acceptée",
                "Félicitations ! Votre demande pour rejoindre « " + nomTontine + " » a été approuvée.",
                "ADHESION",
                "#1f9a5a",
                "/tontine/" + idTontine
        );
    }

    /**
     * Notifie le demandeur que sa demande d'adhésion a bien été envoyée.
     * Le demandeur n'est pas encore membre, donc on utilise la notification liée à User.
     */
    private void envoyerNotificationDemandeur(Users user, Tontine tontine) {
        if (user == null) return;

        String nomTontine = tontine != null ? tontine.getNomTontine() : "la tontine";

        notificationService.creerNotificationUtilisateur(
                user,
                "Demande d'adhésion envoyée",
                "Votre demande pour rejoindre « " + nomTontine + " » a été envoyée. Vous serez notifié dès qu'elle sera traitée.",
                "ADHESION",
                "#0052cc",
                null
        );
    }

    /**
     * Notifie le demandeur que sa demande d'adhésion a été refusée.
     * Le demandeur n'est pas membre, donc on utilise la notification liée à User.
     */
    private void envoyerNotificationRejet(Integer idUser, Tontine tontine) {
        Users user = userRepository.findById(idUser).orElse(null);
        if (user == null) return;

        String nomTontine = tontine != null ? tontine.getNomTontine() : "la tontine";

        notificationService.creerNotificationUtilisateur(
                user,
                "Demande d'adhésion refusée",
                "Votre demande pour rejoindre « " + nomTontine + " » a été refusée.",
                "ADHESION",
                "#e63946",
                null
        );
    }
}
