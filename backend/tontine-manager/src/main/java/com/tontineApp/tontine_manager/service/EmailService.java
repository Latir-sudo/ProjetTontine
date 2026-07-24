package com.tontineApp.tontine_manager.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    public void sendResetCodeEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("Réinitialisation de votre mot de passe - Tontine App");
            helper.setText(buildResetEmailHtml(code), true);

            mailSender.send(message);
            log.info("Email de réinitialisation envoyé à {}", toEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Erreur lors de l'envoi de l'email à {} : {}", toEmail, e.getMessage());
        }
    }

    private String buildResetEmailHtml(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f6fb;">
              <div style="max-width:500px;margin:40px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                <div style="background:linear-gradient(135deg,#0052cc,#003d99);padding:32px 24px;text-align:center;">
                  <h1 style="color:#ffffff;margin:0;font-size:22px;">Tontine App</h1>
                  <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">Réinitialisation de mot de passe</p>
                </div>
                <div style="padding:32px 24px;">
                  <p style="color:#333;font-size:15px;line-height:1.6;margin:0 0 20px;">
                    Bonjour,<br><br>
                    Vous avez demandé la réinitialisation de votre mot de passe. Voici votre code de vérification :
                  </p>
                  <div style="background:#f0f4ff;border:2px dashed #0052cc;border-radius:12px;padding:20px;text-align:center;margin:24px 0;">
                    <span style="font-size:32px;font-weight:bold;letter-spacing:8px;color:#0052cc;">%s</span>
                  </div>
                  <p style="color:#666;font-size:13px;line-height:1.5;margin:20px 0 0;">
                    Ce code expire dans <strong>15 minutes</strong>.<br>
                    Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.
                  </p>
                </div>
                <div style="background:#f8f9fc;padding:16px 24px;text-align:center;border-top:1px solid #e6e9f2;">
                  <p style="color:#999;font-size:12px;margin:0;">&copy; Tontine App - Ne pas répondre à cet email</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(code);
    }
}
