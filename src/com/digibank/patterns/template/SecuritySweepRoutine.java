package com.digibank.patterns.template;

import com.digibank.patterns.observer.SensorSystem;
import com.digibank.patterns.singleton.AuditLogger;

public class SecuritySweepRoutine extends DailyRoutineTemplate {
    private final SensorSystem sensorSystem;

    public SecuritySweepRoutine(SensorSystem sensorSystem) {
        this.sensorSystem = sensorSystem;
    }

    @Override
    protected void executeRoutine() {
        AuditLogger.getInstance().log("[SecuritySweep] Kritik noktalar kontrol ediliyor.");
        sensorSystem.triggerEvent("FIRE", "Merkez Park");
        sensorSystem.triggerEvent("TRAFFIC_JAM", "Bulvar 1");
    }

    @Override
    protected void postReport() {
        AuditLogger.getInstance().log("[SecuritySweep] Tum ihbarlar ilgili birimlere iletildi.");
    }
}
