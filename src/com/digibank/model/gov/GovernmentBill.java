package com.digibank.model.gov;

import java.math.BigDecimal;

public class GovernmentBill {
    private String billingId;
    private String type; // TAX, TRAFFIC_FINE, UTILITY
    private BigDecimal amount;
    private String date;
    private boolean isPaid;

    public GovernmentBill(String billingId, String type, BigDecimal amount, String date) {
        this.billingId = billingId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.isPaid = false;
    }

    public String getBillingId() { return billingId; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    @Override
    public String toString() {
        return "DevletFaturasi{" + type + " - " + amount + "TL - " + (isPaid ? "ODEME TAMAM" : "ODEME BEKLIYOR") + "}";
    }
}
