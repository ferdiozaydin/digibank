package com.digibank.service;

import com.digibank.model.Transaction;
import com.digibank.model.User;
import com.digibank.patterns.singleton.AuditLogger;
import com.digibank.patterns.strategy.PaymentStrategy;
import com.digibank.patterns.adapter.CryptoAdapter;

import java.math.BigDecimal;

public class PaymentService {
    private final TransactionRepository repository;

    public PaymentService(TransactionRepository repository) {
        this.repository = repository;
    }

    public PaymentService() {
        this(new TransactionRepository());
    }
    
    public boolean processPayment(User user, BigDecimal amount, PaymentStrategy strategy) {
        AuditLogger.getInstance().log("Odeme baslatildi: kullanici=" + user.getUsername());

        boolean success = strategy.pay(user, amount);

        String durum = success ? "BASARILI" : "BASARISIZ";
        Transaction tx = new Transaction(System.currentTimeMillis(), user.getId(), "Odeme", amount, durum);
        AuditLogger.getInstance().log("Islem kaydi olusturuldu: " + tx);
        repository.save(tx);

        if (!success) {
            AuditLogger.getInstance().log("Odeme tamamlanamadi: kullanici=" + user.getUsername());
        }
        return success;
    }

    public boolean processCryptoPayment(User user, BigDecimal amountFiat, CryptoAdapter adapter) {
        AuditLogger.getInstance().log("Kripto odeme baslatildi: kullanici=" + user.getUsername() + " adapter=" + adapter.name());
        boolean success = adapter.pay(user, amountFiat);
        String durum = success ? "BASARILI" : "BASARISIZ";
        Transaction tx = new Transaction(System.currentTimeMillis(), user.getId(), "KriptoOdeme-" + adapter.name(), amountFiat, durum);
        AuditLogger.getInstance().log("Kripto islem kaydi olusturuldu: " + tx);
        repository.save(tx);
        if (!success) {
            AuditLogger.getInstance().log("Kripto odeme tamamlanamadi: kullanici=" + user.getUsername());
        }
        return success;
    }
}
