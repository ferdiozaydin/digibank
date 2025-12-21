package com.digibank.patterns.observer;

public class PublicUtilityService implements Observer {
    @Override
    public void update(String eventType, String location) {
        if ("WATER_LEAK".equals(eventType) || "POWER_OUTAGE".equals(eventType)) {
            System.out.println("[UtilityService] Bakim ekibi yonlendirildi -> " + location + " (olay=" + eventType + ")");
        }
    }
}
