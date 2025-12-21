package com.digibank.patterns.observer;

public class BankingNotificationService implements Observer {
    @Override
    public void update(String eventType, String location) {
        System.out.println("[BankingNotificationService] " + location + " icin " + eventType + " konusunda anlik bildirim gonderiliyor");
    }
}
