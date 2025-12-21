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

    public Long getUserId() { return userId; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }

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
