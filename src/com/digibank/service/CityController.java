package com.digibank.service;

import com.digibank.patterns.observer.SensorSystem;
import com.digibank.patterns.command.CommandInvoker;
import com.digibank.patterns.template.DailyRoutineTemplate;
import com.digibank.patterns.template.LightingRoutine;
import com.digibank.patterns.template.SecuritySweepRoutine;
import com.digibank.service.HomeDeviceController;

public class CityController {
    private SensorSystem sensorSystem;
    private CommandInvoker commandInvoker;
    private InfrastructureController infrastructureController;
    private HomeDeviceController homeDeviceController;

    public CityController(SensorSystem sensorSystem, CommandInvoker invoker, InfrastructureController infrastructureController, HomeDeviceController homeDeviceController) {
        this.sensorSystem = sensorSystem;
        this.commandInvoker = invoker;
        this.infrastructureController = infrastructureController;
        this.homeDeviceController = homeDeviceController;
    }

    public void simulateCityEvents() {
        System.out.println("--- CityController: Sehir durum incelemesi ---");
        // Simule olaylar
        sensorSystem.triggerEvent("TRAFFIC_JAM", "Broadway Ave");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) { e.printStackTrace(); }
        sensorSystem.triggerEvent("FIRE", "Downtown 5th St");
        sensorSystem.triggerEvent("WATER_LEAK", "Rezidans A Blok");
    }

    public void runDailyRoutines() {
        DailyRoutineTemplate lighting = new LightingRoutine(infrastructureController, "Merkez Cadde");
        DailyRoutineTemplate security = new SecuritySweepRoutine(sensorSystem);
        lighting.runRoutine();
        security.runRoutine();
    }

    public void executeQueuedCommands() {
        if (commandInvoker != null) {
            commandInvoker.runAll();
        }
    }

    public void demoHomeAutomation() {
        if (homeDeviceController != null) {
            homeDeviceController.toggleDevice("LIGHT-LIVINGROOM", true);
            homeDeviceController.setThermostat("Salon", 22.5);
        }
    }
}
