package com.digibank.patterns.strategy;

import com.digibank.model.User;
import java.math.BigDecimal;

public interface PaymentStrategy {
    boolean pay(User user, BigDecimal amount);
}
