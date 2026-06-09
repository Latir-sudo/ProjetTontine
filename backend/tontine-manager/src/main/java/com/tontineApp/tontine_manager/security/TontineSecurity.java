package com.tontineApp.tontine_manager.security;

import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.model.Tontine;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import com.tontineApp.tontine_manager.repository.TontineRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("tontineSecurity")
@RequiredArgsConstructor
public class TontineSecurity {

    private final TontineRepository tontineRepository;
    private final MembreRepository membreRepository;
    private final UserRepository userRepository;

    private Users getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    // Vérifier si l'utilisateur est l'ADMIN de la tontine
    public boolean isAdmin(Authentication authentication, Integer tontineId) {
        Users user = getCurrentUser(authentication);
        if (user == null) return false;

        Tontine tontine = tontineRepository.findById(tontineId).orElse(null);
        if (tontine == null) return false;

        return tontine.getAdmin() != null && tontine.getAdmin().getId().equals(user.getId());
    }

    // Vérifier si l'utilisateur est MEMBRE de la tontine
    public boolean isMember(Authentication authentication, Integer tontineId) {
        Users user = getCurrentUser(authentication);
        if (user == null) return false;

        return membreRepository.existsByTontine_IdAndUser_Id(tontineId, user.getId());
    }

    // Vérifier si l'utilisateur est ADMIN ou MEMBRE
    public boolean isAdminOrMember(Authentication authentication, Integer tontineId) {
        return isAdmin(authentication, tontineId) || isMember(authentication, tontineId);
    }
}