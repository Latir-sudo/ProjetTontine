package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.dto.PaiementHistoriqueResponse;
import com.tontineApp.tontine_manager.dto.PaiementRequest;
import com.tontineApp.tontine_manager.dto.PaiementResponse;
import com.tontineApp.tontine_manager.dto.PaiementStatsResponse;
import com.tontineApp.tontine_manager.enumeration.StatutCotisation;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.mapper.PaiementMapper;
import com.tontineApp.tontine_manager.model.Cotisation;
import com.tontineApp.tontine_manager.model.Paiement;
import com.tontineApp.tontine_manager.repository.CotisationRepository;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import com.tontineApp.tontine_manager.repository.PaiementRepository;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tontineApp.tontine_manager.enumeration.StatutCotisation.*;

@Slf4j
@Service
@AllArgsConstructor
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final CotisationRepository cotisationRepository;
    private final PaiementMapper paiementMapper;
    private final MembreRepository membreRepository;
    private final TontineRepository tontineRepository;

    // ========== MÉTHODES DE RECHERCHE ==========

    /**
     * Récupère l'historique des paiements d'un membre
     */
    public List<PaiementHistoriqueResponse> getHistoriqueByMembre(Integer membreId) {
        log.info("Récupération historique paiements pour membre: {}", membreId);

        if (membreId == null) {
            log.warn("membreId est null");
            return List.of();
        }

        List<Paiement> paiements = paiementRepository.findByMembreIdOrderByDatePaiementDesc(membreId);
        log.info("{} paiement(s) trouvé(s) pour le membre {}", paiements.size(), membreId);

        if (paiements.isEmpty()) {
            return List.of();
        }

        return paiements.stream()
                .map(paiement -> {
                    try {
                        return paiementMapper.toHistoriqueResponse(paiement);
                    } catch (Exception e) {
                        log.error("Erreur mapping paiement {}: {}", paiement.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Récupère l'historique des paiements d'une tontine
     */
    public List<PaiementHistoriqueResponse> getHistoriqueByTontine(Integer tontineId) {
        log.info("Récupération historique paiements pour tontine: {}", tontineId);

        if (tontineId == null) {
            log.warn("tontineId est null");
            return List.of();
        }

        // Vérifier que la tontine existe
        if (!tontineRepository.existsById(tontineId)) {
            log.warn("Tontine {} non trouvée", tontineId);
            throw new RessourceNotFoundException("Tontine non trouvée avec l'id: " + tontineId);
        }

        List<Paiement> paiements = paiementRepository.findByTontineIdOrderByDatePaiementDesc(tontineId);
        log.info("{} paiement(s) trouvé(s) pour la tontine {}", paiements.size(), tontineId);

        if (paiements.isEmpty()) {
            log.info("Aucun paiement pour la tontine {}", tontineId);
            return List.of();
        }

        return paiements.stream()
                .map(paiement -> {
                    try {
                        return paiementMapper.toHistoriqueResponse(paiement);
                    } catch (Exception e) {
                        log.error("Erreur mapping paiement {}: {}", paiement.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les paiements d'une cotisation
     */
    public List<PaiementResponse> getPaiementsByCotisation(Integer cotisationId) {
        log.info("Récupération paiements pour cotisation: {}", cotisationId);

        if (cotisationId == null) {
            return List.of();
        }

        List<Paiement> paiements = paiementRepository.findByCotisation_Id(cotisationId);
        log.info("{} paiement(s) trouvé(s) pour la cotisation {}", paiements.size(), cotisationId);

        return paiements.stream()
                .map(paiementMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un paiement par son ID
     */
    public PaiementResponse getPaiementById(Integer paiementId) {
        log.info("Récupération paiement par id: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RessourceNotFoundException("Paiement non trouvé avec l'id: " + paiementId));

        return paiementMapper.toResponse(paiement);
    }

    /**
     * Récupère tous les paiements validés
     */
    public List<PaiementResponse> getAllPaiementsValides() {
        log.info("Récupération de tous les paiements validés");

        List<Paiement> paiements = paiementRepository.findAllValides();
        return paiements.stream()
                .map(paiementMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupère tous les paiements en attente
     */
    public List<PaiementResponse> getAllPaiementsEnAttente() {
        log.info("Récupération de tous les paiements en attente");

        List<Paiement> paiements = paiementRepository.findAllEnAttente();
        return paiements.stream()
                .map(paiementMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ========== MÉTHODES DE CRÉATION ET MODIFICATION ==========

    /**
     * Crée un nouveau paiement pour une cotisation
     */
    @Transactional
    public PaiementResponse createPaiement(PaiementRequest request, Integer cotisationId) {
        log.info("Création d'un nouveau paiement pour la cotisation: {}", cotisationId);

        Cotisation cotisation = cotisationRepository.findById(cotisationId)
                .orElseThrow(() -> new RessourceNotFoundException("Cotisation non trouvée avec l'id: " + cotisationId));

        Paiement paiement = new Paiement();
        paiement.setMontant(request.getMontant());
        paiement.setDatePaiement(request.getDatePaiement() != null ? request.getDatePaiement() : new Date());
        paiement.setModePaiement(request.getModePaiement());
        paiement.setReference(request.getReference());
        paiement.setValide(false);
        paiement.setCotisation(cotisation);

        Paiement saved = paiementRepository.save(paiement);
        log.info("Paiement créé avec succès, id={}", saved.getId());

        // Mettre à jour le statut de la cotisation
        updateCotisationStatut(cotisation);

        return paiementMapper.toResponse(saved);
    }

    /**
     * Valide un paiement (par un admin)
     */
    @Transactional
    public PaiementResponse validerPaiement(Integer paiementId) {
        log.info("Validation du paiement: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RessourceNotFoundException("Paiement non trouvé avec l'id: " + paiementId));

        paiement.setValide(true);
        Paiement saved = paiementRepository.save(paiement);
        log.info("Paiement {} validé avec succès", paiementId);

        // Mettre à jour le statut de la cotisation
        updateCotisationStatut(paiement.getCotisation());

        return paiementMapper.toResponse(saved);
    }

    /**
     * Rejette un paiement (par un admin)
     */
    @Transactional
    public PaiementResponse rejeterPaiement(Integer paiementId) {
        log.info("Rejet du paiement: {}", paiementId);

        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RessourceNotFoundException("Paiement non trouvé avec l'id: " + paiementId));

        paiement.setValide(false);
        Paiement saved = paiementRepository.save(paiement);
        log.info("Paiement {} rejeté", paiementId);

        return paiementMapper.toResponse(saved);
    }

    /**
     * Supprime un paiement
     */
    @Transactional
    public void deletePaiement(Integer paiementId) {
        log.info("Suppression du paiement: {}", paiementId);

        if (!paiementRepository.existsById(paiementId)) {
            throw new RessourceNotFoundException("Paiement non trouvé avec l'id: " + paiementId);
        }

        paiementRepository.deleteById(paiementId);
        log.info("Paiement {} supprimé avec succès", paiementId);
    }

    // ========== MÉTHODES STATISTIQUES ==========

    /**
     * Récupère les statistiques des paiements d'un membre
     */
    public PaiementStatsResponse getStatsByMembre(Integer membreId) {
        log.info("Calcul des statistiques de paiement pour le membre: {}", membreId);

        // Récupérer toutes les cotisations du membre
        List<Cotisation> cotisations = cotisationRepository.findByMembre_Id(membreId);
        Long totalAttendu = cotisations.stream().mapToLong(Cotisation::getMontant).sum();

        // Récupérer les paiements validés
        List<Paiement> paiementsValides = paiementRepository.findValidesByMembreId(membreId);
        Long totalPaye = paiementsValides.stream().mapToLong(Paiement::getMontant).sum();

        Integer paiementsReussis = paiementRepository.countValidesByMembreId(membreId);
        Integer paiementsEnAttente = paiementRepository.countEnAttenteByMembreId(membreId);
        Integer paiementsEchoues = paiementRepository.countEchouesByMembreId(membreId);

        Double tauxCompletude = totalAttendu > 0 ? (totalPaye.doubleValue() / totalAttendu.doubleValue()) * 100 : 0;

        log.info("Statistiques membre {}: totalPaye={}, totalAttendu={}, taux={}%",
                membreId, totalPaye, totalAttendu, tauxCompletude);

        return new PaiementStatsResponse(
                totalPaye, totalAttendu, paiementsReussis, paiementsEnAttente, paiementsEchoues, tauxCompletude
        );
    }

    /**
     * Récupère les statistiques des paiements d'une tontine
     */
    public PaiementStatsResponse getStatsByTontine(Integer tontineId) {
        log.info("Calcul des statistiques de paiement pour la tontine: {}", tontineId);

        if (!tontineRepository.existsById(tontineId)) {
            throw new RessourceNotFoundException("Tontine non trouvée avec l'id: " + tontineId);
        }

        Long totalPaye = paiementRepository.sumMontantValidesByTontineId(tontineId);
        if (totalPaye == null) totalPaye = 0L;

        Integer paiementsReussis = paiementRepository.countValidesByTontineId(tontineId);
        Integer paiementsEnAttente = paiementRepository.countEnAttenteByTontineId(tontineId);

        // Calcul du total attendu (somme de toutes les cotisations de la tontine)
        Long totalAttendu = paiementRepository.sumMontantValidesByTontineId(tontineId);
        if (totalAttendu == null) totalAttendu = 0L;

        Double tauxCompletude = totalAttendu > 0 ? (totalPaye.doubleValue() / totalAttendu.doubleValue()) * 100 : 0;

        return new PaiementStatsResponse(
                totalPaye, totalAttendu, paiementsReussis, paiementsEnAttente, 0, tauxCompletude
        );
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Met à jour le statut d'une cotisation en fonction des paiements validés
     */
    private void updateCotisationStatut(Cotisation cotisation) {
        List<Paiement> paiements = paiementRepository.findByCotisation_Id(cotisation.getId());

        Long totalPaye = paiements.stream()
                .filter(p -> p.getValide() != null && p.getValide())
                .mapToLong(Paiement::getMontant)
                .sum();

        StatutCotisation newStatut;

        if (totalPaye >= cotisation.getMontant()) {
            newStatut = COMPLET;
        } else if (totalPaye > 0) {
            newStatut = PARTIEL;
        } else {
            newStatut = EN_ATTENTE;
        }

        if (!newStatut.equals(cotisation.getStatut())) {
            cotisation.setStatut(newStatut);
            cotisationRepository.save(cotisation);
            log.info("Statut de la cotisation {} mis à jour: {}", cotisation.getId(), newStatut);
        }
    }

    /**
     * Vérifie si l'utilisateur est propriétaire du membre
     */
    public boolean isMembreOwner(String email, Integer membreId) {
        log.debug("Vérification si l'utilisateur {} est propriétaire du membre {}", email, membreId);

        return membreRepository.findById(membreId)
                .map(membre -> membre.getUser() != null && membre.getUser().getEmail().equals(email))
                .orElse(false);
    }

    /**
     * Vérifie si un paiement existe
     */
    public boolean existsPaiement(Integer paiementId) {
        return paiementRepository.existsById(paiementId);
    }

    /**
     * Vérifie si une référence de paiement existe déjà
     */
    public boolean existsByReference(String reference) {
        return paiementRepository.existsByReference(reference);
    }
}