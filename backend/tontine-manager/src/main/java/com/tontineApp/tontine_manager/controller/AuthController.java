package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.AuthResponse;
import com.tontineApp.tontine_manager.dto.LoginRequest;
import com.tontineApp.tontine_manager.dto.UserRequest;
import com.tontineApp.tontine_manager.dto.UserResponse;
import com.tontineApp.tontine_manager.service.JwtService;
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

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;  // ← Ajouter
    private final UserDetailsService userDetailsService;

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
}