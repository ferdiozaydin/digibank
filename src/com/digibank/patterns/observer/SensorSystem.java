package com.digibank.patterns.observer;

import com.digibank.patterns.singleton.AuditLogger;
import java.util.ArrayList;
import java.util.List;

public class SensorSystem {
    private List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String eventType, String location) {
        AuditLogger.getInstance().log("Sensor dogrulama: " + eventType + " icin dinleyicilere bildirim yapiliyor, konum=" + location);
        for (Observer observer : observers) {
            observer.update(eventType, location);
        }
    }

    public void triggerEvent(String eventType, String location) {
        System.out.println(">>> SENSOR ALGILADI: " + eventType + " - Konum: " + location);
        notifyObservers(eventType, location);
    }
}
