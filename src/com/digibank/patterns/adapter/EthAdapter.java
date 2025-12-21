package com.digibank.patterns.adapter;

import com.digibank.model.User;
import com.digibank.patterns.singleton.AuditLogger;
import java.math.BigDecimal;

public class EthAdapter implements CryptoAdapter {
    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("0.0003"); // 1 fiat -> 0.0003 ETH (demo)

    @Override
    public boolean pay(User user, BigDecimal amountFiat) {
        BigDecimal cryptoNeeded = amountFiat.multiply(EXCHANGE_RATE);
        if (user.getCryptoBalance().compareTo(cryptoNeeded) >= 0) {
            user.setCryptoBalance(user.getCryptoBalance().subtract(cryptoNeeded));
            AuditLogger.getInstance().log("[ETH Adapter] Odeme basarili. Fiat=" + amountFiat + " ETH=" + cryptoNeeded);
            return true;
        }
        AuditLogger.getInstance().log("[ETH Adapter] Odeme basarisiz. Yetersiz ETH.");
        return false;
    }

    @Override
    public String name() {
        return "ETH";
    }
}
