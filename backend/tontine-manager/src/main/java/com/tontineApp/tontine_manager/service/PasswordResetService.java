package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.model.PasswordResetToken;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.PasswordResetTokenRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public String generateResetCode(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte associé à cet email"));

        tokenRepository.deleteByEmail(email);
        tokenRepository.flush();

        String code = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCode(code);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        token.setUsed(false);

        tokenRepository.saveAndFlush(token);

        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendResetCodeEmail(email, code);
            } catch (Exception e) {
                log.error("Erreur envoi email (non bloquant) : {}", e.getMessage());
            }
        });
        log.info("Code de réinitialisation généré pour {}", email);

        return code;
    }

    public boolean verifyCode(String email, String code) {
        log.info("Vérification du code pour email={}, code={}", email, code);

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByEmailAndCodeAndUsedFalse(email, code);

        if (tokenOpt.isEmpty()) {
            log.warn("Aucun token trouvé pour email={}, code={}", email, code);
            return false;
        }

        PasswordResetToken token = tokenOpt.get();
        if (token.isExpired()) {
            log.warn("Token expiré pour email={}", email);
            return false;
        }

        log.info("Code valide pour email={}", email);
        return true;
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        PasswordResetToken token = tokenRepository.findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new IllegalArgumentException("Code invalide ou expiré"));

        if (token.isExpired()) {
            throw new IllegalArgumentException("Le code a expiré. Veuillez en demander un nouveau.");
        }

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        user.setUserPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        log.info("Mot de passe réinitialisé avec succès pour {}", email);
    }
}
