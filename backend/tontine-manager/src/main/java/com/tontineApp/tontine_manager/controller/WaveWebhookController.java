package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.service.payment.WaveCheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/wave")
@RequiredArgsConstructor
public class WaveWebhookController {
    private final WaveCheckoutService waveCheckoutService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> receive(@RequestHeader(value = "Wave-Signature", required = false) String signature,
                                        @RequestBody String rawBody) {
        waveCheckoutService.processWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
