package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.MembreRequest;
import com.tontineApp.tontine_manager.service.MembreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/membres")
@RequiredArgsConstructor
public class MembreController {

    private final MembreService membreService;

    @GetMapping("/tontine/{idTontine}")
    @PreAuthorize("isAuthenticated()")
    public List<MembreRequest> getMembresByTontine(@PathVariable Integer idTontine) {
        return membreService.getMembresByTontine(idTontine);
    }

    // ADMIN DE LA TONTINE : ajouter un membre
    @PostMapping
    @PreAuthorize("isAuthenticated() and @tontineSecurity.isAdmin(#authentication, #request.idTontine)")
    public ResponseEntity<?> ajouterUtilisateurATontine(@RequestBody MembreRequest request, Authentication authentication) {
        try {
            MembreRequest nouveauMembre = membreService.ajouterUtilisateurATontine(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Utilisateur " + nouveauMembre.getIdUser() + " ajouté avec succès à la tontine");
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "error");
            return ResponseEntity.badRequest().body(error);
        }
    }


}
