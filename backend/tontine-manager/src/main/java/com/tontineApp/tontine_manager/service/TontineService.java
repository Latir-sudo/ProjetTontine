package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.dto.MembreRequest;
import com.tontineApp.tontine_manager.dto.TontineRequest;
import com.tontineApp.tontine_manager.dto.TontineResponse;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.mapper.TontineMapper;
import com.tontineApp.tontine_manager.model.Tontine;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TontineService {
    private final TontineRepository tontineRepository;
    private final TontineMapper tontineMapper;
    private final UserRepository userRepository;
    private final MembreService membreService;

    public List<TontineResponse> getAllTontines() {
        log.info("Récupération de toutes les tontines");
        return tontineRepository.findAll().stream()
                .map(tontineMapper::toTontineResponse)
                .toList();
    }

    public List<TontineResponse> getParRegion(String region) {
        log.info("Récupération des tontines par région: {}", region);
        return tontineRepository.findByRegionTontine(region).stream()
                .map(tontineMapper::toTontineResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TontineResponse> getTontinesByMemberEmail(String email) {
        log.info("Récupération des tontines pour l'email: {}", email);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        return tontineRepository.findTontinesByUserId(user.getId()).stream()
                .map(tontineMapper::toTontineResponse)
                .collect(Collectors.toList());
    }

    public List<TontineResponse> getByCategorie(String categorie) {
        log.info("Récupération des tontines par catégorie: {}", categorie);
        return tontineRepository.findByCategorieTontine(categorie).stream()
                .map(tontineMapper::toTontineResponse)
                .toList();
    }

    public TontineResponse getById(Integer id) {
        log.info("Récupération de la tontine avec l'ID: {}", id);
        return tontineMapper.toTontineResponse(
            tontineRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Tontine non trouvée avec l'ID: " + id))
        );
    }

    @Transactional(readOnly = true)
    public List<TontineResponse> getTontinesByUserId(Integer userId) {
        log.info("Récupération des tontines pour l'utilisateur ID: {}", userId);
        return tontineRepository.findTontinesByUserId(userId).stream()
                .map(tontineMapper::toTontineResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TontineResponse> getMesTontines(String email) {
        log.info("Récupération de mes tontines pour: {}", email);
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvé avec l'email: " + email));
        return getTontinesByUserId(user.getId());
    }

    @Transactional
    public TontineResponse save(TontineRequest tontineRequest, String adminEmail) {
        log.info("Création d'une nouvelle tontine par: {}", adminEmail);
        
        // Vérifier que la requête n'est pas null
        if (tontineRequest == null) {
            throw new IllegalArgumentException("La requête tontine ne peut pas être null");
        }
        
        // Récupérer l'administrateur
        Users admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouvé avec l'email: " + adminEmail));

        // Créer et configurer la tontine
        Tontine tontine = tontineMapper.toTontine(tontineRequest);
        tontine.setAdmin(admin);
        tontine.setDateCreation(LocalDate.now());
        tontine.setStatutTontine("ACTIVE");
        tontine.setNombreMembres(0);
        
        // Valider les données
        validateTontine(tontine);

        // Sauvegarder la tontine
        Tontine savedTontine = tontineRepository.save(tontine);
        log.info("Tontine sauvegardée avec l'ID: {}", savedTontine.getId());
        
        // Ajouter l'admin comme membre
        try {
            MembreRequest membreRequest = new MembreRequest(admin.getId(), savedTontine.getId(), LocalDate.now());
            membreService.ajouterUtilisateurATontine(membreRequest);
            log.info("Admin ajouté comme membre de la tontine");
            
            // Incrémenter le nombre de membres
            savedTontine.setNombreMembres(1);
            savedTontine = tontineRepository.save(savedTontine);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout de l'admin comme membre: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'ajout du membre à la tontine", e);
        }

        return tontineMapper.toTontineResponse(savedTontine);
    }

    @Transactional
    public void delete(Integer id) {
        log.info("Suppression de la tontine avec l'ID: {}", id);
        
        Tontine tontine = tontineRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Tontine non trouvée avec l'ID: " + id));
        
        // Vérifier si la tontine peut être supprimée
        if (tontine.getNombreMembres() > 1) {
            throw new IllegalStateException("Impossible de supprimer une tontine qui a des membres");
        }
        
        tontineRepository.deleteById(id);
        log.info("Tontine supprimée avec succès");
    }

    @Transactional
    public TontineResponse update(TontineRequest tontineRequest, Integer id) {
        log.info("Mise à jour de la tontine avec l'ID: {}", id);
        
        if (tontineRequest == null) {
            throw new IllegalArgumentException("La requête tontine ne peut pas être null");
        }
        
        Tontine tontine = tontineRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Tontine non trouvée avec l'ID: " + id));

        // Mettre à jour les champs non null
        if (tontineRequest.getDescriptionTontine() != null && !tontineRequest.getDescriptionTontine().isBlank()) {
            tontine.setDescriptionTontine(tontineRequest.getDescriptionTontine());
        }
        
        if (tontineRequest.getPolitiqueTontine() != null && !tontineRequest.getPolitiqueTontine().isBlank()) {
            tontine.setPolitiqueTontine(tontineRequest.getPolitiqueTontine());
        }
        
        if (tontineRequest.getNomTontine() != null && !tontineRequest.getNomTontine().isBlank()) {
            tontine.setNomTontine(tontineRequest.getNomTontine());
        }
        
        if (tontineRequest.getFrequence() != null && !tontineRequest.getFrequence().isBlank()) {
            tontine.setFrequence(tontineRequest.getFrequence());
        }
        
        if (tontineRequest.getMontant() != null && tontineRequest.getMontant() > 0) {
            tontine.setMontant(tontineRequest.getMontant());
        }
        
        if (tontineRequest.getRegion() != null && !tontineRequest.getRegion().isBlank()) {
            tontine.setRegionTontine(tontineRequest.getRegion());
        }

        if (tontineRequest.getCategorie() != null && !tontineRequest.getCategorie().isBlank()) {
            tontine.setCategorieTontine(tontineRequest.getCategorie());
        }
        
        if (tontineRequest.getNombreMax() != null && tontineRequest.getNombreMax() > 0) {
            tontine.setNombreMax(tontineRequest.getNombreMax());
        }

        // Sauvegarder la tontine mise à jour
        Tontine updatedTontine = tontineRepository.save(tontine);
        log.info("Tontine mise à jour avec succès");
        
        return tontineMapper.toTontineResponse(updatedTontine);
    }

    private void validateTontine(Tontine tontine) {
        if (tontine.getNomTontine() == null || tontine.getNomTontine().isBlank()) {
            throw new IllegalArgumentException("Le nom de la tontine est requis");
        }
        
        if (tontine.getMontant() == null || tontine.getMontant() <= 0) {
            throw new IllegalArgumentException("Le montant de la cotisation doit être supérieur à 0");
        }
        
        if (tontine.getNombreMax() == null || tontine.getNombreMax() <= 0) {
            throw new IllegalArgumentException("Le nombre maximum de membres doit être supérieur à 0");
        }
        
        if (tontine.getFrequence() == null || tontine.getFrequence().isBlank()) {
            throw new IllegalArgumentException("La fréquence de la tontine est requise");
        }
    }
}
