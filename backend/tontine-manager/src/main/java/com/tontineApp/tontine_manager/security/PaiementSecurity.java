package com.tontineApp.tontine_manager.security;

import com.tontineApp.tontine_manager.service.PaiementService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("paiementSecurity")
@AllArgsConstructor
public class PaiementSecurity {

    private final PaiementService paiementService;

    public boolean isOwner(Authentication authentication, Integer membreId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String currentUserEmail = authentication.getName();
        return paiementService.isMembreOwner(currentUserEmail, membreId);
    }
}