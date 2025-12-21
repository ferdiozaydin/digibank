package com.digibank.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import com.digibank.model.User;
import com.digibank.model.gov.GovernmentBill;
import com.digibank.service.PaymentService;
import com.digibank.service.AuthenticationService;
import com.digibank.service.PredictiveAnalyticsService;
import com.digibank.service.HomeDeviceController;
import com.digibank.integration.SmartGovernmentService;
import com.digibank.service.UserRepository;
import com.digibank.util.SimpleJson;
import com.digibank.patterns.strategy.FiatPaymentStrategy;
import com.digibank.patterns.adapter.BtcAdapter;
import com.digibank.patterns.adapter.EthAdapter;
import com.digibank.patterns.adapter.StablecoinAdapter;
import com.digibank.patterns.adapter.CryptoAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigDecimal;
import java.util.Scanner;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiServer {

    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final SmartGovernmentService smartGovService;
    private final AuthenticationService authService;
    private final PredictiveAnalyticsService predictiveService;
    private final HomeDeviceController homeDeviceController;
    private final Map<String, User> tokenStore = new ConcurrentHashMap<>();
    private final String devBypassToken = System.getenv().getOrDefault("DEV_AUTH_TOKEN", "");
    private final boolean requireHttps = Boolean.parseBoolean(System.getenv().getOrDefault("REQUIRE_HTTPS", "false"));

    public ApiServer(UserRepository userRepository, PaymentService paymentService, SmartGovernmentService smartGovService, AuthenticationService authService, PredictiveAnalyticsService predictiveService, HomeDeviceController homeDeviceController) {
        this.userRepository = userRepository;
        this.paymentService = paymentService;
        this.smartGovService = smartGovService;
        this.authService = authService;
        this.predictiveService = predictiveService;
        this.homeDeviceController = homeDeviceController;
    }

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/user", new UserHandler());
        server.createContext("/api/users", new UsersHandler());
        server.createContext("/api/users/register", new UserRegisterHandler());
        server.createContext("/api/bills", new BillsHandler());
        server.createContext("/api/pay", new PaymentHandler());
        server.createContext("/api/export", new ExportHandler());
        server.createContext("/api/metrics", new MetricsHandler());
        server.createContext("/api/forecast", new ForecastHandler());
        server.createContext("/api/home/toggle", new HomeToggleHandler());
        server.createContext("/api/home/thermostat", new HomeThermostatHandler());
        
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("API sunucusu " + port + " portunda calisiyor");
    }

    class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "RESIDENT", "ADMIN")) return;
            if ("GET".equals(t.getRequestMethod())) {
                String response = SimpleJson.toJson(authed);
                sendResponse(t, 200, response);
            } else {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
            }
        }
    }

    class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireRole(t, authed, "ADMIN")) return;
            if ("GET".equals(t.getRequestMethod())) {
                String response = SimpleJson.toJson(userRepository.findAll());
                sendResponse(t, 200, response);
            } else {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
            }
        }
    }

    class UserRegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireRole(t, authed, "ADMIN")) return;
            if (!"POST".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
                return;
            }
            String body = readBody(t.getRequestBody());
            String username = extractValue(body, "username");
            String password = extractValue(body, "password");
            String role = extractValue(body, "role");
            if (username == null || password == null) {
                sendResponse(t, 400, "{\"hata\":\"Eksik bilgiler\"}");
                return;
            }
            if (role == null || role.isEmpty()) role = "RESIDENT";

            if (userRepository.findByUsername(username) != null) {
                sendResponse(t, 409, "{\"hata\":\"Kullanici zaten var\"}");
                return;
            }

            User newUser = new User(null, username, "", "", "", "", role.toUpperCase(), new BigDecimal("0"), new BigDecimal("0"));
            // demo totp secret accepts 000000
            authService.provisionCredentials(newUser, password, "DEMO_USER_SECRET", newUser.getRole());
            userRepository.upsert(newUser);
            sendResponse(t, 201, "{\"durum\":\"BASARILI\"}");
        }
    }

    class BillsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "RESIDENT", "ADMIN")) return;
            if ("GET".equals(t.getRequestMethod())) {
                // Fetch bills for mocked citizen
                List<GovernmentBill> bills = smartGovService.fetchBills("12345678901");
                String response = SimpleJson.toJson(bills);
                sendResponse(t, 200, response);
            } else {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
            }
        }
    }

    class PaymentHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "RESIDENT", "ADMIN")) return;
            if ("POST".equals(t.getRequestMethod())) {
                String body = readBody(t.getRequestBody());
                System.out.println("Odeme istegi govdesi: " + body);
                
                // Very simple parsing "billId":"TAX-..."
                // In real world use a parser. Here we manually extract for demo without libs.
                String billId = extractValue(body, "billId");
                String payMode = extractValue(body, "payMode"); // FIAT/BTC/ETH/STABLE
                
                if (billId != null) {
                    List<GovernmentBill> bills = smartGovService.fetchBills("12345678901");
                    GovernmentBill targetBill = null;
                    for(GovernmentBill b : bills) {
                        if(b.getBillingId().equals(billId)) {
                             targetBill = b; break;
                        }
                    }

                    if (targetBill != null) {
                       boolean success;
                       CryptoAdapter adapter = selectAdapter(payMode);
                       if (adapter != null) {
                           success = smartGovService.payBillWithCrypto(authed, targetBill, adapter);
                       } else {
                           success = smartGovService.payBill(authed, targetBill);
                       }
                       if (success) {
                           String response = "{\"durum\":\"BASARILI\"}";
                           sendResponse(t, 200, response);
                       } else {
                           sendResponse(t, 402, "{\"hata\":\"Yetersiz bakiye veya islem reddedildi\"}");
                       }
                    } else {
                        sendResponse(t, 404, "{\"hata\":\"Fatura bulunamadi\"}");
                    }
                } else {
                    sendResponse(t, 400, "{\"hata\":\"Gecersiz veri\"}");
                }
            } else {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
            }
        }
        }

    class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
                return;
            }

            String body = readBody(t.getRequestBody());
            String username = extractValue(body, "username");
            String password = extractValue(body, "password");
            String totp = extractValue(body, "totp");

            if (username == null || password == null || totp == null) {
                sendResponse(t, 400, "{\"hata\":\"Eksik bilgiler\"}");
                return;
            }

            User found = userRepository.findByUsername(username);
            if (found == null) {
                sendResponse(t, 401, "{\"hata\":\"Kullanici veya sifre hatali\"}");
                return;
            }

            boolean ok = authService.login(found, password, totp);
            if (!ok) {
                sendResponse(t, 401, "{\"hata\":\"Kimlik dogrulama basarisiz\"}");
                return;
            }

            String token = UUID.randomUUID().toString();
            tokenStore.put(token, found);
            String response = "{\"token\":\"" + token + "\"}";
            sendResponse(t, 200, response);
        }
    }


    class ExportHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireRole(t, authed, "ADMIN")) return;
            if ("POST".equals(t.getRequestMethod())) {
                try {
                    Path dir = Paths.get("txt");
                    if (!Files.exists(dir)) Files.createDirectories(dir);

                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    String filename = "user_export_" + timestamp + ".txt";
                    Path file = dir.resolve(filename);

                    StringBuilder content = new StringBuilder();
                    content.append("DISARI AKTARIM TARIHI: ").append(timestamp).append("\n");
                    content.append("--------------------------------------------------\n");
                    content.append("ID | KULLANICI | TL | KRIPTO\n");
                    content.append("--------------------------------------------------\n");
                          content.append(authed.getId()).append(" | ")
                              .append(authed.getUsername()).append(" | ")
                              .append(authed.getFiatBalance()).append(" | ")
                              .append(authed.getCryptoBalance()).append("\n");
                    content.append("2 | Şinasi Ordu | 12000.00 | 1.2\n");
                    content.append("3 | Kutay Ballı | 150.50 | 0.01\n");
                    
                    Files.write(file, content.toString().getBytes());

                    String response = "{\"durum\":\"BASARILI\", \"dosya\":\"" + filename + "\"}";
                    sendResponse(t, 200, response);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(t, 500, "{\"hata\":\"Disari aktarma basarisiz\"}");
                }
            } else {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
            }
        }
    }

    class MetricsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "ADMIN")) return;
            if ("GET".equals(t.getRequestMethod())) {
                String response = SimpleJson.toJson(predictiveService.metricsSnapshot());
                sendResponse(t, 200, response);
            } else {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
            }
        }
    }

    class ForecastHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "ADMIN")) return;
            if ("GET".equals(t.getRequestMethod())) {
                String type = extractQueryParam(t.getRequestURI().getQuery(), "type");
                String response;
                if ("energy".equalsIgnoreCase(type)) {
                    response = SimpleJson.toJson(predictiveService.forecastEnergy());
                } else {
                    response = SimpleJson.toJson(predictiveService.forecastTraffic());
                }
                sendResponse(t, 200, response);
            } else {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
            }
        }
    }

    class HomeToggleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "RESIDENT", "ADMIN")) return;
            if (!"POST".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
                return;
            }
            String body = readBody(t.getRequestBody());
            String deviceId = extractValue(body, "deviceId");
            String onStr = extractValue(body, "on");
            boolean on = "true".equalsIgnoreCase(onStr) || "1".equals(onStr);
            if (deviceId == null) {
                sendResponse(t, 400, "{\"hata\":\"Eksik cihaz bilgisi\"}");
                return;
            }
            homeDeviceController.toggleDevice(deviceId, on);
            sendResponse(t, 200, "{\"durum\":\"BASARILI\"}");
        }
    }

    class HomeThermostatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "RESIDENT", "ADMIN")) return;
            if (!"POST".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
                return;
            }
            String body = readBody(t.getRequestBody());
            String zone = extractValue(body, "zone");
            String targetStr = extractValue(body, "target");
            if (zone == null || targetStr == null) {
                sendResponse(t, 400, "{\"hata\":\"Eksik parametre\"}");
                return;
            }
            try {
                double target = Double.parseDouble(targetStr);
                homeDeviceController.setThermostat(zone, target);
                sendResponse(t, 200, "{\"durum\":\"BASARILI\"}");
            } catch (NumberFormatException e) {
                sendResponse(t, 400, "{\"hata\":\"Gecersiz hedef sicaklik\"}");
            }
        }
    }

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        t.getResponseHeaders().add("Content-Type", "application/json");
        t.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // CORS for dev
        byte[] body = response.getBytes();
        t.sendResponseHeaders(statusCode, body.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(body);
        }
    }

    private String readBody(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    private String extractValue(String json, String key) {
        // Primitive string find for "key":"value"
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        
        // find start quote
        int quoteStart = json.indexOf("\"", start);
        if (quoteStart == -1) return null;
        
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1) return null;
        
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private CryptoAdapter selectAdapter(String mode) {
        if (mode == null) return null;
        switch (mode.toUpperCase()) {
            case "BTC":
                return new BtcAdapter();
            case "ETH":
                return new EthAdapter();
            case "STABLE":
                return new StablecoinAdapter();
            default:
                return null;
        }
    }

    private String extractQueryParam(String query, String key) {
        if (query == null) return null;
        String[] parts = query.split("&");
        for (String part : parts) {
            String[] kv = part.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }

    private User requireAuth(HttpExchange t) throws IOException {
        if (requireHttps) {
            String proto = t.getRequestHeaders().getFirst("X-Forwarded-Proto");
            if (proto == null || !proto.equalsIgnoreCase("https")) {
                sendResponse(t, 400, "{\"hata\":\"HTTPS gerekli\"}");
                return null;
            }
        }
        String header = t.getRequestHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            User u = tokenStore.get(token);
            if (u != null) return u;
        }
        // Developer bypass token for local demos
        if (!devBypassToken.isEmpty()) {
            String token = header != null ? header.replace("Bearer ", "") : "";
            if (devBypassToken.equals(token)) {
                User admin = userRepository.findByUsername("admin");
                return admin;
            }
        }
        sendResponse(t, 401, "{\"hata\":\"Yetkisiz: Token gerekli\"}");
        return null;
    }

    private boolean requireRole(HttpExchange t, User authed, String role) throws IOException {
        if (authed == null) return false;
        if (authed.hasRole(role)) return true;
        sendResponse(t, 403, "{\"hata\":\"Erisim yasak: Rol gerekli\"}");
        return false;
    }

    private boolean requireAnyRole(HttpExchange t, User authed, String... roles) throws IOException {
        if (authed == null) return false;
        for (String r : roles) {
            if (authed.hasRole(r)) return true;
        }
        sendResponse(t, 403, "{\"hata\":\"Erisim yasak: Rol gerekli\"}");
        return false;
    }
}
