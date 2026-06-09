package com.tontineApp.tontine_manager.security;

import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.CotisationRepository;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("cotisationSecurity")
@RequiredArgsConstructor
public class CotisationSecurity {

    private final CotisationRepository cotisationRepository;
    private final UserRepository userRepository;
    private final MembreRepository membreRepository;
    private final TontineSecurity tontineSecurity;

    private Users getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
    public boolean isOwner(Authentication authentication, Integer idUser, Integer idTontine) {
        Users currentUser = getCurrentUser(authentication);
        if (currentUser == null) {
            System.out.println("isOwner(3 params): utilisateur non trouvé");
            return false;
        }

        System.out.println("isOwner(3 params): currentUserId=" + currentUser.getId() + ", idUser=" + idUser + ", idTontine=" + idTontine);

        // 1. Vérifier que l'utilisateur connecté est celui dont on demande les cotisations
        if (!currentUser.getId().equals(idUser)) {
            System.out.println("isOwner(3 params): utilisateur différent");
            return false;
        }

        // 2. Vérifier que l'utilisateur est membre de cette tontine
        boolean isMember = membreRepository.existsByTontine_IdAndUser_Id(idTontine, idUser);
        System.out.println("isOwner(3 params): est membre? " + isMember);

        return isMember;
    }

    // Méthode existante - vérifie par l'ID de la cotisation
    public boolean isOwner(Authentication authentication, Integer cotisationId) {
        Users user = getCurrentUser(authentication);
        if (user == null) return false;

        var cotisation = cotisationRepository.findById(cotisationId).orElse(null);
        if (cotisation == null) return false;

        return cotisation.getMembre().getUser().getId().equals(user.getId());
    }

    // Vérifier si l'utilisateur peut voir la cotisation (admin de la tontine ou propriétaire)
    public boolean canView(Authentication authentication, Integer cotisationId) {
        Users user = getCurrentUser(authentication);
        if (user == null) return false;

        var cotisation = cotisationRepository.findById(cotisationId).orElse(null);
        if (cotisation == null) return false;

        Integer tontineId = cotisation.getMembre().getTontine().getId();

        return tontineSecurity.isAdmin(authentication, tontineId) || isOwner(authentication, cotisationId);
    }
}