package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.model.Role;
import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)  // ← AJOUTER CETTE ANNOTATION
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ✅ Utiliser findByEmailWithRoles au lieu de findByEmail
        Users user = userRepository.findByEmailWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new UsernameNotFoundException("L'utilisateur n'a pas de rôle assigné: " + username);
        }

        return new User(
                user.getEmail(),
                user.getUserPassword(),
                getGrantedAuthority(user.getRoles())
        );
    }

    public List<GrantedAuthority> getGrantedAuthority(List<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNomRole()))
                .collect(Collectors.toList());
    }
}