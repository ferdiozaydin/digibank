package com.digibank.patterns.adapter;

import com.digibank.model.User;
import com.digibank.patterns.singleton.AuditLogger;
import java.math.BigDecimal;

public class BtcAdapter implements CryptoAdapter {
    private static final BigDecimal EXCHANGE_RATE = new BigDecimal("0.0001"); // 1 fiat -> 0.0001 BTC

    @Override
    public boolean pay(User user, BigDecimal amountFiat) {
        BigDecimal cryptoNeeded = amountFiat.multiply(EXCHANGE_RATE);
        if (user.getCryptoBalance().compareTo(cryptoNeeded) >= 0) {
            user.setCryptoBalance(user.getCryptoBalance().subtract(cryptoNeeded));
            AuditLogger.getInstance().log("[BTC Adapter] Odeme basarili. Fiat=" + amountFiat + " BTC=" + cryptoNeeded);
            return true;
        }
        AuditLogger.getInstance().log("[BTC Adapter] Odeme basarisiz. Yetersiz BTC.");
        return false;
    }

    @Override
    public String name() {
        return "BTC";
    }
}
