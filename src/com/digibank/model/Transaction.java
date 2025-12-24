package com.digibank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private Long id;
    private Long userId;
    private String description;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status;

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

    @Override
    public String toString() {
        return "Islem{" +
            "id=" + id +
            ", kullaniciId=" + userId +
            ", aciklama='" + description + '\'' +
            ", tutar=" + amount +
            ", durum='" + status + '\'' +
            ", zaman=" + timestamp +
            '}';
    }
}
