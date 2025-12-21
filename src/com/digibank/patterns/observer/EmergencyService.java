package com.digibank.patterns.observer;

public class EmergencyService implements Observer {
    @Override
    public void update(String eventType, String location) {
        if ("FIRE".equals(eventType) || "ACCIDENT".equals(eventType)) {
            System.out.println("[EmergencyService] " + eventType + " nedeni ile ekibimiz " + location + " icin yonlendirildi");
        }
    }
}
