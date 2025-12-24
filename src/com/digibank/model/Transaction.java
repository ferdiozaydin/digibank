package com.digibank.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private Long userId;
    private String description;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status;

    // Yeni alanlar (manuel kayıt formu için)
    private String fullName;
    private String bankCode;
    private String address;
    private LocalDate recordDate;

    public Transaction(Long id, Long userId, String description, BigDecimal amount, String status) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(Long id, Long userId, String description, BigDecimal amount, String status, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public Transaction(Long id, String fullName, BigDecimal amount, String bankCode, String address, LocalDate recordDate) {
        this.id = id;
        this.fullName = fullName;
        this.amount = amount;
        this.bankCode = bankCode;
        this.address = address;
        this.recordDate = recordDate;
        this.status = "BASARILI";
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    @Override
    public String toString() {
        return "Islem{" +
            "id=" + id +
            ", kullaniciId=" + userId +
            ", aciklama='" + description + '\'' +
            ", adSoyad='" + fullName + '\'' +
            ", bankaKodu='" + bankCode + '\'' +
            ", adres='" + address + '\'' +
            ", kayitTarihi=" + recordDate +
            ", tutar=" + amount +
            ", durum='" + status + '\'' +
            ", zaman=" + timestamp +
            '}';
    }
}
