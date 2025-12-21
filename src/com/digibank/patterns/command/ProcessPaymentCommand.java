package com.digibank.patterns.command;

import com.digibank.model.User;
import com.digibank.service.PaymentService;
import com.digibank.patterns.strategy.PaymentStrategy;
import java.math.BigDecimal;

public class ProcessPaymentCommand implements Command {
    private final PaymentService paymentService;
    private final User user;
    private final BigDecimal amount;
    private final PaymentStrategy strategy;

    public ProcessPaymentCommand(PaymentService paymentService, User user, BigDecimal amount, PaymentStrategy strategy) {
        this.paymentService = paymentService;
        this.user = user;
        this.amount = amount;
        this.strategy = strategy;
    }

    @Override
    public void execute() {
        paymentService.processPayment(user, amount, strategy);
    }
}
