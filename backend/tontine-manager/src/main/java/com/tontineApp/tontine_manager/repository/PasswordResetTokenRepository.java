package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    @Modifying
    void deleteByEmail(String email);
}
