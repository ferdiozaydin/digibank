package com.digibank.patterns.command;

import com.digibank.service.HomeDeviceController;

public class ToggleHomeDeviceCommand implements Command {
    private final HomeDeviceController controller;
    private final String deviceId;
    private final boolean on;

    public ToggleHomeDeviceCommand(HomeDeviceController controller, String deviceId, boolean on) {
        this.controller = controller;
        this.deviceId = deviceId;
        this.on = on;
    }

    @Override
    public void execute() {
        controller.toggleDevice(deviceId, on);
    }
}
