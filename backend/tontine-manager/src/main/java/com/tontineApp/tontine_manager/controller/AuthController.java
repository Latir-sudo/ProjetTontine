package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.*;
import com.tontineApp.tontine_manager.service.JwtService;
import com.tontineApp.tontine_manager.service.PasswordResetService;
import com.tontineApp.tontine_manager.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;  // ← Ajouter
    private final UserDetailsService userDetailsService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            // Token contient déjà l'utilisateur, on ne renvoie que le token
            AuthResponse response = new AuthResponse(true, "Connexion réussie", token, null);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Email ou mot de passe incorrect", null, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRequest userRequest) {
        try {
            // 1. Vérifier si l'email existe déjà
            if (userService.existsByEmail(userRequest.getEmail())) {
                AuthResponse errorResponse = new AuthResponse(
                        false,
                        "Cet email est déjà utilisé",
                        null,
                        null
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // 2. Créer l'utilisateur (le mot de passe est encrypté dans le service)
            UserResponse newUser = userService.saveUser(userRequest);

            // 3. Générer le token AVEC l'utilisateur complet dedans
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(newUser.getEmail())
                    .password(userRequest.getPassword())
                    .authorities(newUser.getRoles().toArray(new String[0]))
                    .build();

            String token = jwtService.generateToken(userDetails);  // Le token contient l'objet user

            // 4. Retourner la réponse (l'utilisateur est optionnel car déjà dans le token)
            AuthResponse response = new AuthResponse(
                    true,
                    "Inscription réussie",
                    token,
                    newUser  // Optionnel, Angular peut décoder depuis le token
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            AuthResponse errorResponse = new AuthResponse(
                    false,
                    "Erreur lors de l'inscription: " + e.getMessage(),
                    null,
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody PasswordResetRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            passwordResetService.generateResetCode(request.getEmail().trim());
            response.put("success", true);
            response.put("message", "Un code de réinitialisation a été envoyé à votre adresse email");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi. Veuillez réessayer.");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<Map<String, Object>> verifyResetCode(@RequestBody PasswordResetVerifyRequest request) {
        try {
            boolean valid = passwordResetService.verifyCode(request.getEmail().trim(), request.getCode().trim());
            Map<String, Object> response = new HashMap<>();
            response.put("success", valid);
            response.put("message", valid ? "Code valide" : "Code invalide ou expiré");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la vérification");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody PasswordResetConfirmRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            passwordResetService.resetPassword(request.getEmail().trim(), request.getCode().trim(), request.getNewPassword());
            response.put("success", true);
            response.put("message", "Mot de passe réinitialisé avec succès");
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la réinitialisation");
        }
        return ResponseEntity.ok(response);
    }
}