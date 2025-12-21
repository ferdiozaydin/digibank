package com.digibank.patterns.command;

import com.digibank.service.InfrastructureController;

public class AdjustTrafficSignalCommand implements Command {
    private final InfrastructureController controller;
    private final String intersection;
    private final String mode;

    public AdjustTrafficSignalCommand(InfrastructureController controller, String intersection, String mode) {
        this.controller = controller;
        this.intersection = intersection;
        this.mode = mode;
    }

    @Override
    public void execute() {
        controller.adjustTrafficSignal(intersection, mode);
    }
}
