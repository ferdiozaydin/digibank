package com.digibank.service;

import com.digibank.patterns.singleton.AuditLogger;

/**
 * Receiver class for infrastructure-related commands (street lights, traffic signals, etc.).
 */
public class InfrastructureController {

    public void turnOnStreetLight(String location) {
        AuditLogger.getInstance().log("Sokak lambasi acildi: " + location);
    }

    public void turnOffStreetLight(String location) {
        AuditLogger.getInstance().log("Sokak lambasi kapatildi: " + location);
    }

    public void adjustTrafficSignal(String intersection, String mode) {
        AuditLogger.getInstance().log("Trafik isigi ayarlandi: " + intersection + " -> " + mode);
    }
}
