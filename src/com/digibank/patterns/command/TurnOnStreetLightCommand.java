package com.digibank.patterns.command;

import com.digibank.service.InfrastructureController;

public class TurnOnStreetLightCommand implements Command {
    private final InfrastructureController controller;
    private final String location;

    public TurnOnStreetLightCommand(InfrastructureController controller, String location) {
        this.controller = controller;
        this.location = location;
    }

    @Override
    public void execute() {
        controller.turnOnStreetLight(location);
    }
}
