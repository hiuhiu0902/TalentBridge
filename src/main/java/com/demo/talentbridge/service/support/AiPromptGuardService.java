package com.demo.talentbridge.service.support;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class AiPromptGuardService {

    private static final List<String> BLOCKED_PHRASES = List.of(
            "password", "mat khau", "mật khẩu",
            "admin password", "mat khau admin", "mật khẩu admin",
            "tai khoan admin", "tài khoản admin",
            "token", "access token", "refresh token",
            "secret", "secret key", "bi mat", "bí mật",
            "api key", "apikey", "private key", "credentials",
            "dump database", "database dump", "full database", "toan bo db", "toàn bộ db",
            "raw sql", "select * from users", "select * from user",
            "drop table", "delete from", "truncate table",
            "other user's private", "private data of",
            "du lieu nguoi dung khac", "dữ liệu người dùng khác",
            "email cua nguoi khac", "sdt cua nguoi khac",
            "hash password", "password hash", "jwt secret"
    );

    public boolean isDeniedPrompt(String message) {
        if (message == null || message.isBlank()) {
            return true;
        }

        String normalized = normalizeText(message);
        return BLOCKED_PHRASES.stream().anyMatch(normalized::contains);
    }

    public String normalizeText(String input) {
        String lower = input.toLowerCase(Locale.ROOT).trim();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("\\s+", " ");
    }
}