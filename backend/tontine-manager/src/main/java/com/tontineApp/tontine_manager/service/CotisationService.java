package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.dto.CotisationRequest;
import com.tontineApp.tontine_manager.dto.CotisationResponse;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.mapper.CotisationMapper;
import com.tontineApp.tontine_manager.model.Cotisation;
import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.model.Tontine;
import com.tontineApp.tontine_manager.repository.CotisationRepository;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tontineApp.tontine_manager.enumeration.StatutCotisation.COMPLET;
import static com.tontineApp.tontine_manager.enumeration.StatutCotisation.PARTIEL;

@Slf4j
@Service
@AllArgsConstructor
public class CotisationService {

    private final CotisationRepository cotisationRepository;
    private final MembreRepository membreRepository;
    private final CotisationMapper cotisationMapper;
    private final TontineRepository tontineRepository;

    @Transactional(readOnly = true)
    public List<CotisationResponse> getCotisations(Integer userId, Integer tontineId) {
        log.debug("Récupération des cotisations pour user={}, tontine={}", userId, tontineId);

        // Vérifier que l'utilisateur existe
        if (userId == null || tontineId == null) {
            log.error("userId ou tontineId est null");
            return List.of();
        }

        Membre membre = membreRepository.findByTontine_IdAndUser_Id(tontineId, userId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        String.format("Aucun membre trouvé pour user=%d dans la tontine=%d", userId, tontineId)
                ));

        List<Cotisation> cotisations = cotisationRepository.findByMembre_Id(membre.getId());

        if (cotisations == null || cotisations.isEmpty()) {
            log.info("Aucune cotisation trouvée pour le membre {}", membre.getId());
            return List.of();
        }

        return cotisations.stream()
                .map(cotisation -> {
                    try {
                        return cotisationMapper.toCotisationResponse(cotisation);
                    } catch (Exception e) {
                        log.error("Erreur lors du mapping de la cotisation {}: {}", cotisation.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(response -> response != null)
                .toList();
    }

    @Transactional(readOnly = true)
    public CotisationResponse getCotisationById(Integer id) {
        Cotisation cotisation = cotisationRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Cotisation " + id + " non trouvée!"));
        return cotisationMapper.toCotisationResponse(cotisation);
    }

    @Transactional
    public void deleteCotisation(Integer id) {
        if (!cotisationRepository.existsById(id)) {
            throw new RessourceNotFoundException("Cotisation " + id + " non trouvée!");
        }
        cotisationRepository.deleteById(id);
        log.info("Cotisation {} supprimée avec succès", id);
    }

    @Transactional
    public CotisationResponse createCotisation(CotisationRequest request, Integer userId, Integer tontineId) {
        log.info("Création d'une cotisation pour user={}, tontine={}, montant={}",
                userId, tontineId, request.getMontant());

        // 1. Récupérer le membre
        Membre membre = membreRepository.findByTontine_IdAndUser_Id(tontineId, userId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        String.format("Membre non trouvé pour user=%d dans la tontine=%d", userId, tontineId)
                ));

        // 2. Vérifier que le montant ne dépasse pas le montant de la tontine
        Integer montantTontine = membre.getTontine().getMontant();
        if (request.getMontant() > montantTontine) {
            throw new IllegalArgumentException(
                    String.format("Le montant (%d) ne peut pas dépasser le montant de la tontine (%d)",
                            request.getMontant(), montantTontine)
            );
        }

        // 3. Créer la cotisation
        Cotisation cotisation = cotisationMapper.toCotisation(request);
        cotisation.setMembre(membre);

        // récupération de la tontine

        Tontine tontine = tontineRepository.findById(tontineId).orElseThrow(()->new RessourceNotFoundException("tontine "+tontineId + " not found"));
        cotisation.setTontine(tontine);
        // 4. Définir le statut
        if (request.getMontant().equals(montantTontine)) {
            cotisation.setStatut(COMPLET);
            log.debug("Cotisation complète pour membre {}", membre.getId());
        } else {
            cotisation.setStatut(PARTIEL);
            log.debug("Cotisation partielle pour membre {}", membre.getId());
        }

        Cotisation saved = cotisationRepository.save(cotisation);
        log.info("Cotisation créée avec succès, id={}", saved.getId());

        return cotisationMapper.toCotisationResponse(saved);
    }
}