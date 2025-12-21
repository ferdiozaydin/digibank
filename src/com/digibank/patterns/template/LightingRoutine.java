package com.digibank.patterns.template;

import com.digibank.service.InfrastructureController;
import com.digibank.patterns.singleton.AuditLogger;

public class LightingRoutine extends DailyRoutineTemplate {
    private final InfrastructureController controller;
    private final String location;

    public LightingRoutine(InfrastructureController controller, String location) {
        this.controller = controller;
        this.location = location;
    }

    @Override
    protected void executeRoutine() {
        controller.turnOffStreetLight(location);
        controller.turnOnStreetLight(location);
        AuditLogger.getInstance().log("[LightingRoutine] Gun dogumu/gun batimi takvimi calistirildi: " + location);
    }
}
