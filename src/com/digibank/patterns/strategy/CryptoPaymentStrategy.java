package com.digibank.patterns.strategy;

import com.digibank.model.User;
import com.digibank.patterns.singleton.AuditLogger;
import java.math.BigDecimal;

public class CryptoPaymentStrategy implements PaymentStrategy {
    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("0.0001"); // 1 Unit = 0.0001 BTC example

    @Override
    public boolean pay(User user, BigDecimal amount) {
        BigDecimal cryptoAmount = amount.multiply(EXCHANGE_RATE);
        if (user.getCryptoBalance().compareTo(cryptoAmount) >= 0) {
            user.setCryptoBalance(user.getCryptoBalance().subtract(cryptoAmount));
            AuditLogger.getInstance().log("Kripto odeme basarili. Fiat karsiligi: " + amount + ", Kripto: " + cryptoAmount);
            return true;
        } else {
            AuditLogger.getInstance().log("Kripto odeme basarisiz. Yetersiz bakiye.");
            return false;
        }
    }
}
