package com.digibank.integration;

import com.digibank.model.User;
import com.digibank.model.gov.GovernmentBill;
import com.digibank.patterns.singleton.AuditLogger;
import com.digibank.service.PaymentService;
import com.digibank.patterns.strategy.FiatPaymentStrategy;
import com.digibank.patterns.adapter.CryptoAdapter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SmartGovernmentService {
    
    private PaymentService paymentService;

    public SmartGovernmentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public List<GovernmentBill> fetchBills(String citizenId) {
        // Simulation: Fetching bills from external E-Government API
        AuditLogger.getInstance().log("Devlet faturalari cekiliyor: Vatandas ID=" + citizenId);
        
        List<GovernmentBill> bills = new ArrayList<>();
        bills.add(new GovernmentBill("TAX-2024-001", "Gelir Vergisi", new BigDecimal("1500.00"), "2025-12-01"));
        bills.add(new GovernmentBill("TRF-2024-992", "Trafik Cezası", new BigDecimal("450.00"), "2025-11-20"));
        
        return bills;
    }

    public boolean payBill(User user, GovernmentBill bill) {
        AuditLogger.getInstance().log("Fatura odemesi baslatildi: " + bill.getBillingId());
        
        // Use existing PaymentService with Fiat Strategy
        FiatPaymentStrategy fiatStrategy = new FiatPaymentStrategy();
        
        // In a real scenario, we might have a specific GovPaymentStrategy
        boolean success = paymentService.processPayment(user, bill.getAmount(), fiatStrategy);

        if (success) {
            bill.setPaid(true);
            AuditLogger.getInstance().log("Fatura " + bill.getBillingId() + " SmartGov uzerinde ODENDI olarak isaretlendi.");
            return true;
        }

        AuditLogger.getInstance().log("Fatura odemesi basarisiz: " + bill.getBillingId());
        return false;
    }

    public boolean payBillWithCrypto(User user, GovernmentBill bill, CryptoAdapter adapter) {
        AuditLogger.getInstance().log("Fatura kripto ile odeniyor: " + bill.getBillingId() + " adapter=" + adapter.name());
        boolean success = paymentService.processCryptoPayment(user, bill.getAmount(), adapter);
        if (success) {
            bill.setPaid(true);
            AuditLogger.getInstance().log("Fatura " + bill.getBillingId() + " kripto ile ODENDI olarak isaretlendi.");
            return true;
        }
        AuditLogger.getInstance().log("Kripto fatura odemesi basarisiz: " + bill.getBillingId());
        return false;
    }
}
