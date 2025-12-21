package com.digibank.service;

import com.digibank.patterns.singleton.AuditLogger;

public class HomeDeviceController {
    public void toggleDevice(String deviceId, boolean on) {
        AuditLogger.getInstance().log("Home device " + deviceId + " set to " + (on ? "ON" : "OFF"));
    }

    public void setThermostat(String zone, double target) {
        AuditLogger.getInstance().log("Thermostat " + zone + " target set to " + target + "C");
    }
}
