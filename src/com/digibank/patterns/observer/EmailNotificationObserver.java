package com.digibank.patterns.observer;

public class EmailNotificationObserver implements Observer {
    @Override
    public void update(String eventType, String location) {
        if ("NEW_FILE_DETECTED".equals(eventType)) {
            System.out.println(">>> [Observer] E-posta gonderiliyor: Yeni dosya olustu -> " + location);
            System.out.println("    Konu: Yeni veri aktarimi hazir");
            System.out.println("    Alici: admin@digibank.com");
        }
    }
}
