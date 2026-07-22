package com.tontineApp.tontine_manager.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tontineApp.tontine_manager.dto.MobilePaymentResponse;
import com.tontineApp.tontine_manager.enumeration.ModePaiement;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.model.Cotisation;
import com.tontineApp.tontine_manager.model.Paiement;
import com.tontineApp.tontine_manager.repository.CotisationRepository;
import com.tontineApp.tontine_manager.repository.PaiementRepository;
import com.tontineApp.tontine_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaveCheckoutService {
    private static final String WAVE = "WAVE";
    private final PaiementRepository paiementRepository;
    private final CotisationRepository cotisationRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wave.api.base-url}") private String baseUrl;
    @Value("${wave.api.key}") private String apiKey;
    @Value("${wave.api.signing-secret:}") private String apiSigningSecret;
    @Value("${wave.webhook.secret}") private String webhookSecret;
    @Value("${wave.checkout.success-url}") private String successUrl;
    @Value("${wave.checkout.error-url}") private String errorUrl;

    public MobilePaymentResponse createCheckout(Integer cotisationId, String phoneNumber) {
        requireCheckoutConfiguration();
        Cotisation cotisation = cotisationRepository.findById(cotisationId)
                .orElseThrow(() -> new RessourceNotFoundException("Cotisation introuvable : " + cotisationId));
        if (cotisation.getMontant() == null || cotisation.getMontant() <= 0) {
            throw new IllegalArgumentException("Le montant de la cotisation doit être strictement positif");
        }

        String reference = "TONTINE-" + cotisationId + "-" + UUID.randomUUID();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amount", String.valueOf(cotisation.getMontant()));
        payload.put("currency", "XOF");
        payload.put("client_reference", reference);
        payload.put("success_url", successUrl);
        payload.put("error_url", errorUrl);
        if (phoneNumber != null && !phoneNumber.isBlank()) payload.put("restrict_payer_mobile", phoneNumber.trim());

        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpHeaders headers = authorizedHeaders(body);
            ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/v1/checkout/sessions", HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);
            JsonNode session = objectMapper.readTree(response.getBody());
            String checkoutId = required(session, "id");
            String launchUrl = required(session, "wave_launch_url");

            Paiement paiement = new Paiement();
            paiement.setMontant(cotisation.getMontant());
            paiement.setDatePaiement(new java.util.Date());
            paiement.setModePaiement(ModePaiement.WAVE);
            paiement.setReference(reference);
            paiement.setValide(false);
            paiement.setProviderCheckoutId(checkoutId);
            paiement.setProviderStatus(text(session, "payment_status", "processing"));
            paiement.setCotisation(cotisation);
            paiementRepository.save(paiement);

            return new MobilePaymentResponse(true, checkoutId, WAVE,
                    "Session Wave créée. Ouvrez Wave pour confirmer le paiement.", phoneNumber,
                    cotisation.getMontant(), reference, "PENDING", launchUrl);
        } catch (HttpStatusCodeException ex) {
            log.warn("Wave a refusé la création du checkout: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalArgumentException("Wave a refusé la création du paiement. Vérifiez la configuration ou les données.");
        } catch (Exception ex) {
            log.error("Erreur pendant la création du checkout Wave", ex);
            throw new IllegalStateException("Impossible de contacter Wave pour créer le paiement");
        }
    }

    @Transactional
    public void processWebhook(String rawBody, String signature) {
        if (!verifySignature(rawBody, signature)) throw new IllegalArgumentException("Signature Wave invalide");
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            JsonNode data = root.path("data");
            String checkoutId = required(data, "id");
            Paiement paiement = paiementRepository.findByProviderCheckoutId(checkoutId).orElse(null);
            if (paiement == null) { log.warn("Webhook Wave ignoré: checkout inconnu {}", checkoutId); return; }

            String paymentStatus = text(data, "payment_status", "processing");
            paiement.setProviderStatus(paymentStatus);
            if ("succeeded".equals(paymentStatus) && "complete".equals(text(data, "checkout_status", ""))) {
                String receivedAmount = text(data, "amount", "");
                if (!String.valueOf(paiement.getMontant()).equals(receivedAmount) || !"XOF".equals(text(data, "currency", ""))) {
                    log.error("Webhook Wave rejeté pour {}: montant/devise incohérent", checkoutId);
                    return;
                }
                paiement.setValide(true);
                paiement.setProviderTransactionId(text(data, "transaction_id", null));
            }
            paiementRepository.save(paiement);
        } catch (Exception ex) {
            if (ex instanceof IllegalArgumentException) throw (IllegalArgumentException) ex;
            log.error("Webhook Wave illisible", ex);
            throw new IllegalArgumentException("Webhook Wave invalide");
        }
    }

    public MobilePaymentResponse getCheckoutStatus(String checkoutId, String email) {
        Paiement paiement = paiementRepository.findByProviderCheckoutId(checkoutId)
                .orElseThrow(() -> new RessourceNotFoundException("Paiement Wave introuvable"));
        Integer currentUserId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"))
                .getId();
        if (!paiement.getCotisation().getMembre().getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas consulter ce paiement");
        }
        String status = Boolean.TRUE.equals(paiement.getValide()) ? "SUCCESS" : "PENDING";
        return new MobilePaymentResponse(true, checkoutId, WAVE, "Statut du paiement Wave", null,
                paiement.getMontant(), paiement.getReference(), status, null);
    }

    private void requireCheckoutConfiguration() {
        if (apiKey == null || apiKey.isBlank() || successUrl == null || successUrl.isBlank() || errorUrl == null || errorUrl.isBlank())
            throw new IllegalStateException("Wave n'est pas configuré. Définissez WAVE_API_KEY, WAVE_SUCCESS_URL et WAVE_ERROR_URL.");
    }

    private HttpHeaders authorizedHeaders(String body) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiSigningSecret != null && !apiSigningSecret.isBlank()) {
            String timestamp = String.valueOf(Instant.now().getEpochSecond());
            headers.set("Wave-Signature", "t=" + timestamp + ",v1=" + hmac(timestamp + body, apiSigningSecret));
        }
        return headers;
    }

    private boolean verifySignature(String rawBody, String header) {
        if (webhookSecret == null || webhookSecret.isBlank() || header == null) return false;
        String timestamp = null;
        for (String part : header.split(",")) {
            String[] keyValue = part.trim().split("=", 2);
            if (keyValue.length == 2 && "t".equals(keyValue[0])) timestamp = keyValue[1];
        }
        if (timestamp == null) return false;
        try {
            long eventTime = Long.parseLong(timestamp);
            if (Math.abs(Instant.now().getEpochSecond() - eventTime) > 300) return false;
            String expected = hmac(timestamp + rawBody, webhookSecret);
            for (String part : header.split(",")) {
                String[] keyValue = part.trim().split("=", 2);
                if (keyValue.length == 2 && "v1".equals(keyValue[0]) && MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), keyValue[1].getBytes(StandardCharsets.US_ASCII))) return true;
            }
            return false;
        } catch (Exception ex) { return false; }
    }

    private String hmac(String content, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) result.append(String.format("%02x", value));
        return result.toString();
    }

    private String required(JsonNode node, String field) {
        String value = text(node, field, null);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Réponse Wave incomplète: " + field);
        return value;
    }
    private String text(JsonNode node, String field, String fallback) {
        return node.hasNonNull(field) ? node.get(field).asText() : fallback;
    }
}
