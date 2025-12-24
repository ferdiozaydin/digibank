package com.digibank;

import com.digibank.api.ApiServer;
import com.digibank.model.User;
import com.digibank.integration.SmartGovernmentService;
import com.digibank.patterns.singleton.AuditLogger;
import com.digibank.service.PaymentService;
import com.digibank.service.AuthenticationService;
import com.digibank.service.InfrastructureController;
import com.digibank.service.PredictiveAnalyticsService;
import com.digibank.service.HomeDeviceController;
import com.digibank.service.TransactionRepository;
import com.digibank.service.UserRepository;
import com.digibank.patterns.observer.DirectoryWatcherService;
import com.digibank.patterns.observer.EmailNotificationObserver;
import com.digibank.patterns.observer.SensorSystem;
import com.digibank.patterns.observer.BankingNotificationService;
import com.digibank.patterns.observer.EmergencyService;
import com.digibank.patterns.observer.PublicUtilityService;
import com.digibank.service.CityController;
import com.digibank.patterns.command.CommandInvoker;
import com.digibank.patterns.command.TurnOnStreetLightCommand;
import com.digibank.patterns.command.AdjustTrafficSignalCommand;
import com.digibank.patterns.command.ToggleHomeDeviceCommand;

import java.io.IOException;
import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== DigiBank arka uc servisi baslatiliyor... ===");

        // 1. Initialize Singletons and Services
        AuditLogger logger = AuditLogger.getInstance();
        
        TransactionRepository txRepo = new TransactionRepository();
        PaymentService paymentService = new PaymentService(txRepo);
        AuthenticationService authService = new AuthenticationService();
        UserRepository userRepository = new UserRepository();
        SmartGovernmentService govService = new SmartGovernmentService(paymentService);
        InfrastructureController infrastructureController = new InfrastructureController();
        PredictiveAnalyticsService predictiveService = new PredictiveAnalyticsService();
        HomeDeviceController homeDeviceController = new HomeDeviceController();
        CommandInvoker invoker = new CommandInvoker();
        SensorSystem sensorSystem = new SensorSystem();

        sensorSystem.addObserver(new BankingNotificationService());
        sensorSystem.addObserver(new EmergencyService());
        sensorSystem.addObserver(new PublicUtilityService());

        // 2. Create Single Demo Admin User (In memory DB)
        User admin = new User(1L, "admin", "", "", "", "", "ADMIN", new BigDecimal("5000.00"), new BigDecimal("0.5"));
        // Provision credentials with salted hash and TOTP secret (demo TOTP accepts 000000)
        authService.provisionCredentials(admin, "admin", "DEMO_ADMIN_SECRET", "ADMIN");
        userRepository.upsert(admin);

        // 2.a Start Directory Watcher (Bonus Step 13)
        try {
            DirectoryWatcherService watcher = new DirectoryWatcherService("txt");
            watcher.addObserver(new EmailNotificationObserver());
            new Thread(watcher).start();
        } catch (IOException e) {
            System.err.println("Failed to start Directory Watcher: " + e.getMessage());
        }

        // 3. Command pattern demo queue
        invoker.submit(new TurnOnStreetLightCommand(infrastructureController, "Merkez Cadde"));
        invoker.submit(new AdjustTrafficSignalCommand(infrastructureController, "Kavsak-1", "GREEN_WAVE"));
        invoker.submit(new ToggleHomeDeviceCommand(homeDeviceController, "LIGHT-LIVINGROOM", true));

        // 4. Start API Server
        ApiServer server = new ApiServer(userRepository, txRepo, paymentService, govService, authService, predictiveService, homeDeviceController);
        
        try {
            server.start(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 5. City routines and command execution
        CityController cityController = new CityController(sensorSystem, invoker, infrastructureController, homeDeviceController);
        cityController.runDailyRoutines();
        cityController.simulateCityEvents();
        cityController.executeQueuedCommands();
        cityController.demoHomeAutomation();
        
        // Keep running
        // In Docker this is enough as HttpServer creates a thread.
    }
}
