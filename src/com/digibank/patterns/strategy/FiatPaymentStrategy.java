package com.digibank.patterns.strategy;

import com.digibank.model.User;
import com.digibank.patterns.singleton.AuditLogger;
import java.math.BigDecimal;

public class FiatPaymentStrategy implements PaymentStrategy {
    @Override
    public boolean pay(User user, BigDecimal amount) {
        if (user.getFiatBalance().compareTo(amount) >= 0) {
            user.setFiatBalance(user.getFiatBalance().subtract(amount));
            AuditLogger.getInstance().log("Fiat odeme basarili. Tutar: " + amount);
            return true;
        } else {
            AuditLogger.getInstance().log("Fiat odeme basarisiz. Yetersiz bakiye.");
            return false;
        }
    }
}
