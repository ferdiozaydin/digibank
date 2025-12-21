package com.digibank.service;

import com.digibank.model.User;
import com.digibank.patterns.singleton.AuditLogger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticationService {
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int TOTP_TIME_STEP_SECONDS = 30;
    private static final int TOTP_DIGITS = 6;
    private static final int MAX_FAIL_BEFORE_LOCK = 5;
    private static final long LOCK_WINDOW_MS = 30_000;
    private final Map<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void provisionCredentials(User user, String plainPassword, String totpSecret, String role) {
        String salt = generateSalt();
        String hash = hashPassword(plainPassword, salt);
        user.setSalt(salt);
        user.setPasswordHash(hash);
        user.setTotpSecret(totpSecret);
        user.setRole(role);
        // PQ placeholder to show future post-quantum integration
        user.setPqPublicKey("PQ_PLACEHOLDER_KEY");
        AuditLogger.getInstance().log("Kullanici kimlik bilgileri hazirlandi: " + user.getUsername());
    }

    public boolean login(User user, String password, String totpCode) {
        LoginAttempt attempt = attempts.computeIfAbsent(user.getUsername(), u -> new LoginAttempt());

        if (attempt.isLocked()) {
            AuditLogger.getInstance().log("Hesap kilitli (gecici): " + user.getUsername());
            return false;
        }

        boolean passwordOk = verifyPassword(password, user.getSalt(), user.getPasswordHash());
        boolean totpOk = verifyTotp(user.getTotpSecret(), totpCode);

        if (passwordOk && totpOk) {
            attempt.reset();
            AuditLogger.getInstance().log("Kullanici giris yapti: " + user.getUsername());
            return true;
        }

        attempt.registerFailure();
        long backoff = attempt.backoffMillis();
        AuditLogger.getInstance().log("Giris basarisiz: " + user.getUsername() + " gecikme=" + backoff + "ms");
        try {
            Thread.sleep(backoff);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        AuditLogger.getInstance().log("Giris basarisiz: " + user.getUsername());
        return false;
    }

    public boolean verifyPassword(String password, String salt, String expectedHash) {
        if (password == null || salt == null || expectedHash == null) return false;
        String actualHash = hashPassword(password, salt);
        return constantTimeEquals(expectedHash, actualHash);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-512");
            digest.update(salt.getBytes());
            byte[] hashed = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            AuditLogger.getInstance().logError("Sifre karmasi olusturulamadi", e);
            throw new RuntimeException(e);
        }
    }

    private String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private boolean verifyTotp(String secret, String providedCode) {
        if (secret == null || providedCode == null) return false;
        // Demo backdoor for lab environments where no authenticator app is available
        if (secret.startsWith("DEMO") && "000000".equals(providedCode)) {
            return true;
        }
        long currentBucket = Instant.now().getEpochSecond() / TOTP_TIME_STEP_SECONDS;
        String expected = generateTotp(secret, currentBucket);
        // allow slight clock skew: check previous and next window
        String previous = generateTotp(secret, currentBucket - 1);
        String next = generateTotp(secret, currentBucket + 1);
        return providedCode.equals(expected) || providedCode.equals(previous) || providedCode.equals(next);
    }

    private String generateTotp(String secret, long timeStep) {
        try {
            byte[] keyBytes = secret.getBytes();
            byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);

            int otp = binary % (int) Math.pow(10, TOTP_DIGITS);
            return String.format("%0" + TOTP_DIGITS + "d", otp);
        } catch (Exception e) {
            AuditLogger.getInstance().logError("TOTP uretilemedi", e);
            throw new RuntimeException(e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static class LoginAttempt {
        private int failures = 0;
        private long lockUntilMs = 0;

        void registerFailure() {
            failures++;
            if (failures >= MAX_FAIL_BEFORE_LOCK) {
                lockUntilMs = System.currentTimeMillis() + LOCK_WINDOW_MS;
            }
        }

        boolean isLocked() {
            if (lockUntilMs == 0) {
                return false;
            }
            if (System.currentTimeMillis() > lockUntilMs) {
                reset();
                return false;
            }
            return true;
        }

        long backoffMillis() {
            return Math.min(2000L, failures * 300L);
        }

        void reset() {
            failures = 0;
            lockUntilMs = 0;
        }
    }
}
