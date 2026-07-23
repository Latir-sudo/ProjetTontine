package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.*;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.service.PaiementService;
import com.tontineApp.tontine_manager.service.payment.WaveCheckoutService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
@AllArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;
    private final WaveCheckoutService waveCheckoutService;

    /**
     * Récupère l'historique des paiements d'un membre
     * GET /api/paiements/membre/{membreId}/historique
     */
    @GetMapping("/membre/{membreId}/historique")
    @PreAuthorize("isAuthenticated() and @paiementSecurity.isOwner(#authentication, #membreId)")
    public ResponseEntity<?> getHistoriqueByMembre(
            @PathVariable Integer membreId,
            Authentication authentication) {
        try {
            List<PaiementHistoriqueResponse> historique = paiementService.getHistoriqueByMembre(membreId);
            return ResponseEntity.ok(historique);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'historique"));
        }
    }

    @GetMapping("/user/{userId}/historique")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaiementHistoriqueResponse>> getHistoriqueByUser(
            @PathVariable Integer userId,
            Authentication authentication) {
        List<PaiementHistoriqueResponse> historique = paiementService.getHistoriqueByUser(userId);
        return ResponseEntity.ok(historique);
    }

    @GetMapping("/user/{userId}/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaiementStatsResponse> getStatsByUser(
            @PathVariable Integer userId,
            Authentication authentication) {
        PaiementStatsResponse stats = paiementService.getStatsByUser(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Récupère l'historique des paiements d'une tontine
     * GET /api/paiements/tontine/{tontineId}/historique
     */
    @GetMapping("/tontine/{tontineId}/historique")
    @PreAuthorize("isAuthenticated() and @tontineSecurity.isMember(#authentication, #tontineId)")
    public ResponseEntity<?> getHistoriqueByTontine(
            @PathVariable Integer tontineId,
            Authentication authentication) {

        try {
            List<PaiementHistoriqueResponse> historique = paiementService.getHistoriqueByTontine(tontineId);
            // ✅ Retourne 200 même si la liste est vide
            return ResponseEntity.ok(historique);

        } catch (RessourceNotFoundException e) {
            System.out.println("Tontine non trouvée: {}"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des paiements: {}"+ e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne du serveur"));
        }
    }

    /**
     * Récupère tous les paiements d'une cotisation
     * GET /api/paiements/cotisation/{cotisationId}
     */
    @GetMapping("/cotisation/{cotisationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaiementResponse>> getPaiementsByCotisation(
            @PathVariable Integer cotisationId,
            Authentication authentication) {
        List<PaiementResponse> paiements = paiementService.getPaiementsByCotisation(cotisationId);
        return ResponseEntity.ok(paiements);
    }

    /**
     * Crée un nouveau paiement pour une cotisation
     * POST /api/paiements/cotisation/{cotisationId}
     */
    @PostMapping("/cotisation/{cotisationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaiementResponse> createPaiement(
            @RequestBody PaiementRequest paiementRequest,
            @PathVariable Integer cotisationId,
            Authentication authentication) {
        PaiementResponse response = paiementService.createPaiement(paiementRequest, cotisationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mobile/wave")
    @PreAuthorize("isAuthenticated() and @cotisationSecurity.isOwner(#authentication, #request.cotisationId)")
    public ResponseEntity<MobilePaymentResponse> createWaveCheckout(
            @jakarta.validation.Valid @RequestBody MobilePaymentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(waveCheckoutService.createCheckout(request.getCotisationId(), request.getPhoneNumber()));
    }

    @GetMapping("/mobile/wave/{checkoutId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MobilePaymentResponse> getWaveCheckoutStatus(@PathVariable String checkoutId,
                                                                         Authentication authentication) {
        return ResponseEntity.ok(waveCheckoutService.getCheckoutStatus(checkoutId, authentication.getName()));
    }

    /**
     * Valide un paiement (par un admin)
     * PUT /api/paiements/{paiementId}/valider
     */
    @PutMapping("/{paiementId}/valider")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementResponse> validerPaiement(
            @PathVariable Integer paiementId,
            Authentication authentication) {
        PaiementResponse response = paiementService.validerPaiement(paiementId);
        return ResponseEntity.ok(response);
    }

    /**
     * Rejette un paiement (par un admin)
     * PUT /api/paiements/{paiementId}/rejeter
     */
    @PutMapping("/{paiementId}/rejeter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaiementResponse> rejeterPaiement(
            @PathVariable Integer paiementId,
            Authentication authentication) {
        PaiementResponse response = paiementService.rejeterPaiement(paiementId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les statistiques des paiements d'un membre
     * GET /api/paiements/membre/{membreId}/stats
     */
    @GetMapping("/membre/{membreId}/stats")
    @PreAuthorize("isAuthenticated() and @paiementSecurity.isOwner(#authentication, #membreId)")
    public ResponseEntity<?> getStatsByMembre(
            @PathVariable Integer membreId,
            Authentication authentication) {
        try {
            PaiementStatsResponse stats = paiementService.getStatsByMembre(membreId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.ok(new PaiementStatsResponse(0L, 0L, 0, 0, 0, 0.0));
        }
    }
}
