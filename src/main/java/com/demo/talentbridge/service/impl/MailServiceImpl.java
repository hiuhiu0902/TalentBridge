package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.service.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String mailFrom;

    @Value("${app.password-reset.token-ttl-minutes:30}")
    private long expiryMinutes;

    @Override
    public void sendPasswordResetEmail(String to, String fullName, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("resetLink", resetLink);
            context.setVariable("productName", "TalentBridge");
            context.setVariable("expiryMinutes", expiryMinutes);

            String html = templateEngine.process("password-reset-email", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("Reset your TalentBridge password");
            helper.setText(html, true);

            log.info("Sending password reset email from={} to={}", mailFrom, to);
            javaMailSender.send(message);
            log.info("Password reset email sent successfully to={}", to);

        } catch (MailException | jakarta.mail.MessagingException e) {
            log.error("Failed to send password reset email to={}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending password reset email to={}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}