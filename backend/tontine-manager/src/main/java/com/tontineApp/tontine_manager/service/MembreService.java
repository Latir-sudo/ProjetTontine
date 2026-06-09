package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.dto.MembreRequest;
import com.tontineApp.tontine_manager.dto.MembreResponse;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.mapper.MembreMapper;
import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.model.Tontine;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MembreService {

    private final UserRepository userRepository;
    private final TontineRepository tontineRepository;
    private final MembreRepository membreRepository;
    private final MembreMapper membreMapper;

    @Transactional
    public MembreRequest ajouterUtilisateurATontine(MembreRequest request) {
        Tontine tontine = tontineRepository.findById(request.getIdTontine())
                .orElseThrow(() -> new RessourceNotFoundException("tontine non trouvee"));

        Users user = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur non trouve"));

        boolean dejaMembre = membreRepository.existsByTontineAndUser(tontine, user);
        if (dejaMembre) {
            throw new IllegalArgumentException("Cet utilisateur est deja membre de cette tontine");
        }

        Membre membre = membreMapper.toMembre(request);
        tontine.setNombreMembres((tontine.getNombreMembres() == null ? 0 : tontine.getNombreMembres()) + 1);
        return membreMapper.toMembreRequest(membreRepository.save(membre));
    }

    public List<MembreRequest> getMembresByTontine(Integer idTontine) {
        if (!tontineRepository.existsById(idTontine)) {
            throw new RessourceNotFoundException("tontine non trouvee");
        }

        return membreRepository.findAllByTontine_Id(idTontine)
                .stream()
                .map(membreMapper::toMembreRequest)
                .toList();
    }

    public List<MembreResponse> getMembresDetailsByTontine(Integer idTontine) {
        if (!tontineRepository.existsById(idTontine)) {
            throw new RessourceNotFoundException("tontine non trouvee");
        }

        return membreRepository.findAllByTontine_Id(idTontine)
                .stream()
                .map(this::toMembreResponse)
                .toList();
    }

    @Transactional
    public void supprimerMembre(Integer idTontine, Integer idMembre) {
        Tontine tontine = tontineRepository.findById(idTontine)
                .orElseThrow(() -> new RessourceNotFoundException("tontine non trouvee"));
        Membre membre = membreRepository.findById(idMembre)
                .orElseThrow(() -> new RessourceNotFoundException("membre non trouve"));

        if (membre.getTontine() == null || !idTontine.equals(membre.getTontine().getId())) {
            throw new IllegalArgumentException("Ce membre n'appartient pas a cette tontine");
        }

        if (tontine.getAdmin() != null && membre.getUser() != null && tontine.getAdmin().getId().equals(membre.getUser().getId())) {
            throw new IllegalArgumentException("L'administrateur ne peut pas etre supprime de sa tontine");
        }

        membreRepository.delete(membre);
        tontine.setNombreMembres(Math.max((tontine.getNombreMembres() == null ? 1 : tontine.getNombreMembres()) - 1, 0));
    }

    private MembreResponse toMembreResponse(Membre membre) {
        Users user = membre.getUser();
        Tontine tontine = membre.getTontine();

        return new MembreResponse(
                membre.getId(),
                user != null ? user.getId() : null,
                tontine != null ? tontine.getId() : null,
                user != null ? user.getPrenom() : null,
                user != null ? user.getNom() : null,
                user != null ? user.getTelephone() : null,
                user != null ? user.getEmail() : null,
                membre.getDateAdhesion()
        );
    }
}
