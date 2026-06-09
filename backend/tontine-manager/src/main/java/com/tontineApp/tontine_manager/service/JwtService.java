package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.model.Users;
import com.tontineApp.tontine_manager.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserRepository userRepository;

    private final String jwtkey = "QiAT1hs6emp0qZZqikMrGcJVl/h8cULpkyJfp/cZlgHhsBtlX3iaACqNMA7EnW55pNN4oqppetIcHdZw7hfVhA==";
    private final long EXPIRATION = 86400000;

    @Transactional(readOnly = true)
    public String generateToken(UserDetails userDetails) {
        Users user = userRepository.findByEmailWithRoles(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userDetails.getUsername()));


        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        claims.put("nom", user.getNom());
        claims.put("prenom", user.getPrenom());
        claims.put("telephone", user.getTelephone());
        claims.put("ville", user.getVille());

        // ✅ Convertir LocalDate en String
        if (user.getDateInscription() != null) {
            String dateInscriptionStr = user.getDateInscription()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            claims.put("dateInscription", dateInscriptionStr);
        }

        // ✅ Ajouter les rôles (List<String> est supporté)
        if (user.getRoles() != null) {
            claims.put("roles", user.getRoles().stream()
                    .map(role -> role.getNomRole())
                    .collect(Collectors.toList()));
        }

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtkey));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    public Map<String, Object> extractUserClaims(String token) {
        Claims claims = extractAllClaims(token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", claims.get("id"));
        userInfo.put("email", claims.get("email"));
        userInfo.put("nom", claims.get("nom"));
        userInfo.put("prenom", claims.get("prenom"));
        userInfo.put("telephone", claims.get("telephone"));
        userInfo.put("ville", claims.get("ville"));
        userInfo.put("dateInscription", claims.get("dateInscription"));
        userInfo.put("roles", claims.get("roles"));
        return userInfo;
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}