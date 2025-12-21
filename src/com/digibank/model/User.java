package com.digibank.model;

import java.math.BigDecimal;

public class User {
    private Long id;
    private String username;
    private String passwordHash; // SHA3-512 hash of password+salt
    private String salt; // per-user salt
    private String totpSecret; // shared secret for TOTP MFA
    private String pqPublicKey; // placeholder for PQ key material (future)
    private String role; // RESIDENT, ADMIN, CONTROLLER
    private BigDecimal fiatBalance;
    private BigDecimal cryptoBalance; // In BTC or generic crypto

    public User(Long id, String username, String salt, String passwordHash, String totpSecret, String pqPublicKey, String role, BigDecimal fiatBalance, BigDecimal cryptoBalance) {
        this.id = id;
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.totpSecret = totpSecret;
        this.pqPublicKey = pqPublicKey;
        this.role = role;
        this.fiatBalance = fiatBalance;
        this.cryptoBalance = cryptoBalance;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getFiatBalance() { return fiatBalance; }
    public void setFiatBalance(BigDecimal fiatBalance) { this.fiatBalance = fiatBalance; }
    public BigDecimal getCryptoBalance() { return cryptoBalance; }
    public void setCryptoBalance(BigDecimal cryptoBalance) { this.cryptoBalance = cryptoBalance; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTotpSecret() { return totpSecret; }
    public void setTotpSecret(String totpSecret) { this.totpSecret = totpSecret; }

    public String getPqPublicKey() { return pqPublicKey; }
    public void setPqPublicKey(String pqPublicKey) { this.pqPublicKey = pqPublicKey; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean hasRole(String expectedRole) {
        return expectedRole != null && expectedRole.equalsIgnoreCase(this.role);
    }
}
