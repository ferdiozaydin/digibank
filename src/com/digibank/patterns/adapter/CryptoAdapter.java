package com.digibank.patterns.adapter;

import com.digibank.model.User;
import java.math.BigDecimal;

public interface CryptoAdapter {
    boolean pay(User user, BigDecimal amountFiat);
    String name();
}
