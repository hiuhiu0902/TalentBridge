package com.demo.talentbridge.service;

public interface MailService {
    void sendPasswordResetEmail(String to, String fullName, String resetLink);
}