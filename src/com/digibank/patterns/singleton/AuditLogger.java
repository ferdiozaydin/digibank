package com.digibank.patterns.singleton;

import java.time.LocalDateTime;

public class AuditLogger {
    private static AuditLogger instance;

    private AuditLogger() {
        // Private constructor
    }

    public static synchronized AuditLogger getInstance() {
        if (instance == null) {
            instance = new AuditLogger();
        }
        return instance;
    }

    public void log(String message) {
        System.out.println("[AUDIT LOG " + LocalDateTime.now() + "]: " + message);
    }

    public void logError(String message, Throwable t) {
        System.err.println("[AUDIT ERROR " + LocalDateTime.now() + "]: " + message + " - " + t.getMessage());
    }
}
