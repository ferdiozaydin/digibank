package com.digibank.patterns.adapter;

import com.digibank.model.User;
import com.digibank.patterns.singleton.AuditLogger;
import java.math.BigDecimal;

public class StablecoinAdapter implements CryptoAdapter {
    @Override
    public boolean pay(User user, BigDecimal amountFiat) {
        // Assume 1:1 peg with fiat
        if (user.getCryptoBalance().compareTo(amountFiat) >= 0) {
            user.setCryptoBalance(user.getCryptoBalance().subtract(amountFiat));
            AuditLogger.getInstance().log("[Stablecoin Adapter] Odeme basarili. Tutar=" + amountFiat);
            return true;
        }
        AuditLogger.getInstance().log("[Stablecoin Adapter] Odeme basarisiz. Yetersiz bakiye.");
        return false;
    }

    @Override
    public String name() {
        return "STABLE";
    }
}
