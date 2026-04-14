package com.next2me.next2view.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EmailService {

    private final EmailClient emailClient;
    private final String fromAddress;

    public EmailService(
            @Value("${AZURE_COMMUNICATION_CONNECTION_STRING}") String connectionString,
            @Value("${MAIL_FROM_ADDRESS}") String fromAddress) {
        this.emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        this.fromAddress = fromAddress;
    }

    public void sendPasswordReset(String toEmail, String resetLink, String userName) {
        try {
            String html = """
                <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
                    <div style="background:#1c2333;padding:24px;border-radius:12px 12px 0 0;text-align:center;">
                        <h1 style="color:#fff;margin:0;font-size:24px;">Next<span style="color:#3b82f6;">2</span>View</h1>
                        <p style="color:#94a3b8;margin:4px 0 0;font-size:12px;">CEO Command Center</p>
                    </div>
                    <div style="background:#fff;padding:32px;border-radius:0 0 12px 12px;border:1px solid #e2e8f0;">
                        <h2 style="color:#1c2333;margin:0 0 16px;">Επαναφορά Κωδικού</h2>
                        <p style="color:#64748b;">Γεια σου %s,</p>
                        <p style="color:#64748b;">Λάβαμε αίτημα επαναφοράς του κωδικού σου για το Next2View.</p>
                        <div style="text-align:center;margin:32px 0;">
                            <a href="%s" style="background:#3b82f6;color:#fff;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:700;font-size:16px;">
                                Επαναφορά Κωδικού
                            </a>
                        </div>
                        <p style="color:#94a3b8;font-size:13px;">Ο σύνδεσμος λήγει σε <strong>1 ώρα</strong>.</p>
                        <p style="color:#94a3b8;font-size:13px;">Αν δεν ζήτησες επαναφορά κωδικού, αγνόησε αυτό το email.</p>
                        <hr style="border:none;border-top:1px solid #e2e8f0;margin:24px 0;">
                        <p style="color:#cbd5e1;font-size:11px;text-align:center;">Next2View · Next2me Group · Private & Confidential</p>
                    </div>
                </div>
                """.formatted(userName, resetLink);

            EmailMessage message = new EmailMessage()
                    .setSenderAddress(fromAddress)
                    .setToRecipients(List.of(new EmailAddress(toEmail)))
                    .setSubject("Επαναφορά Κωδικού — Next2View")
                    .setBodyHtml(html);

            emailClient.beginSend(message).getFinalResult();
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
}