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
import com.digibank.service.TransactionRepository;
import com.digibank.model.Transaction;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class ApiServer {

    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final SmartGovernmentService smartGovService;
    private final AuthenticationService authService;
    private final PredictiveAnalyticsService predictiveService;
    private final HomeDeviceController homeDeviceController;
    private final TransactionRepository transactionRepository;
    private final Map<String, User> tokenStore = new ConcurrentHashMap<>();
    private final String devBypassToken = System.getenv().getOrDefault("DEV_AUTH_TOKEN", "");
    private final boolean requireHttps = Boolean.parseBoolean(System.getenv().getOrDefault("REQUIRE_HTTPS", "false"));

    public ApiServer(UserRepository userRepository, TransactionRepository transactionRepository, PaymentService paymentService, SmartGovernmentService smartGovService, AuthenticationService authService, PredictiveAnalyticsService predictiveService, HomeDeviceController homeDeviceController) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
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
        server.createContext("/api/users/search", new UsersSearchHandler());
        server.createContext("/api/users/item", new UserItemHandler());

        server.createContext("/api/transactions", new TransactionsHandler());
        server.createContext("/api/transactions/search", new TransactionsSearchHandler());
        server.createContext("/api/transactions/item", new TransactionItemHandler());
        server.createContext("/api/transactions/create", new TransactionCreateHandler());
        server.createContext("/api/transfer", new TransferHandler());

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

    class UsersSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireRole(t, authed, "ADMIN")) return;
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
                return;
            }
            String q = extractQueryParam(t.getRequestURI().getQuery(), "q");
            q = urlDecode(q);
            String response = SimpleJson.toJson(userRepository.searchByUsername(q));
            sendResponse(t, 200, response);
        }
    }

    class UserItemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireRole(t, authed, "ADMIN")) return;

            String username = extractQueryParam(t.getRequestURI().getQuery(), "username");
            username = urlDecode(username);
            if (username == null || username.isEmpty()) {
                sendResponse(t, 400, "{\"hata\":\"username gerekli\"}");
                return;
            }

            if ("GET".equals(t.getRequestMethod())) {
                User u = userRepository.findByUsername(username);
                if (u == null) {
                    sendResponse(t, 404, "{\"hata\":\"Kullanici bulunamadi\"}");
                    return;
                }
                sendResponse(t, 200, SimpleJson.toJson(u));
                return;
            }

            if ("DELETE".equals(t.getRequestMethod())) {
                boolean ok = userRepository.deleteByUsername(username);
                if (!ok) {
                    sendResponse(t, 404, "{\"hata\":\"Kullanici bulunamadi\"}");
                    return;
                }
                sendResponse(t, 200, "{\"durum\":\"BASARILI\"}");
                return;
            }

            if ("PUT".equals(t.getRequestMethod())) {
                String body = readBody(t.getRequestBody());
                String role = extractValue(body, "role");
                String fiatStr = extractValue(body, "fiatBalance");
                String cryptoStr = extractValue(body, "cryptoBalance");

                User existing = userRepository.findByUsername(username);
                if (existing == null) {
                    sendResponse(t, 404, "{\"hata\":\"Kullanici bulunamadi\"}");
                    return;
                }

                if (role != null && !role.isEmpty()) {
                    existing.setRole(role.toUpperCase());
                }
                if (fiatStr != null && !fiatStr.isEmpty()) {
                    try {
                        existing.setFiatBalance(new BigDecimal(fiatStr));
                    } catch (Exception e) {
                        sendResponse(t, 400, "{\"hata\":\"Gecersiz fiatBalance\"}");
                        return;
                    }
                }
                if (cryptoStr != null && !cryptoStr.isEmpty()) {
                    try {
                        existing.setCryptoBalance(new BigDecimal(cryptoStr));
                    } catch (Exception e) {
                        sendResponse(t, 400, "{\"hata\":\"Gecersiz cryptoBalance\"}");
                        return;
                    }
                }

                userRepository.upsert(existing);
                sendResponse(t, 200, "{\"durum\":\"BASARILI\"}");
                return;
            }

            sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
        }
    }

    class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "RESIDENT", "ADMIN")) return;
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
                return;
            }
            List<Transaction> txs;
            if (authed.hasRole("ADMIN")) {
                txs = transactionRepository.findAll();
            } else {
                txs = transactionRepository.findByUserId(authed.getId());
            }
            sendResponse(t, 200, SimpleJson.toJson(txs));
        }
    }

    class TransactionsSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "RESIDENT", "ADMIN")) return;
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
                return;
            }
            String q = extractQueryParam(t.getRequestURI().getQuery(), "q");
            q = urlDecode(q);
            List<Transaction> txs;
            if (authed.hasRole("ADMIN")) {
                txs = transactionRepository.search(q, null);
            } else {
                txs = transactionRepository.search(q, authed.getId());
            }
            sendResponse(t, 200, SimpleJson.toJson(txs));
        }
    }

    class TransactionItemHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            User authed = requireAuth(t);
            if (authed == null) return;
            if (!requireAnyRole(t, authed, "RESIDENT", "ADMIN")) return;

            String idStr = extractQueryParam(t.getRequestURI().getQuery(), "id");
            if (idStr == null || idStr.isEmpty()) {
                sendResponse(t, 400, "{\"hata\":\"id gerekli\"}");
                return;
            }
            long id;
            try {
                id = Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                sendResponse(t, 400, "{\"hata\":\"Gecersiz id\"}");
                return;
            }

            Transaction tx = transactionRepository.findById(id);
            if (tx == null) {
                sendResponse(t, 404, "{\"hata\":\"Islem bulunamadi\"}");
                return;
            }
            if (!authed.hasRole("ADMIN") && (tx.getUserId() == null || !tx.getUserId().equals(authed.getId()))) {
                sendResponse(t, 403, "{\"hata\":\"Erisim yasak\"}");
                return;
            }

            if ("GET".equals(t.getRequestMethod())) {
                sendResponse(t, 200, SimpleJson.toJson(tx));
                return;
            }

            if ("DELETE".equals(t.getRequestMethod())) {
                // sadece ADMIN silebilsin
                if (!requireRole(t, authed, "ADMIN")) return;
                boolean ok = transactionRepository.delete(id);
                if (!ok) {
                    sendResponse(t, 404, "{\"hata\":\"Islem bulunamadi\"}");
                    return;
                }
                sendResponse(t, 200, "{\"durum\":\"BASARILI\"}");
                return;
            }

            if ("PUT".equals(t.getRequestMethod())) {
                if (!requireRole(t, authed, "ADMIN")) return;
                String body = readBody(t.getRequestBody());

                // Yeni alanlar (manuel kayıt)
                String fullName = extractValue(body, "fullName");
                String bankCode = extractValue(body, "bankCode");
                String address = extractValue(body, "address");
                String recordDateStr = extractValue(body, "recordDate");
                if (fullName != null) tx.setFullName(fullName);
                if (bankCode != null) tx.setBankCode(bankCode);
                if (address != null) tx.setAddress(address);
                if (recordDateStr != null && !recordDateStr.isEmpty()) {
                    try {
                        tx.setRecordDate(LocalDate.parse(recordDateStr));
                    } catch (Exception e) {
                        sendResponse(t, 400, "{\"hata\":\"Gecersiz recordDate\"}");
                        return;
                    }
                }

                // Eski alanlar (geri uyumluluk)
                String desc = extractValue(body, "description");
                String amountStr = extractValue(body, "amount");
                String status = extractValue(body, "status");
                if (desc != null) tx.setDescription(desc);
                if (status != null) tx.setStatus(status);
                if (amountStr != null && !amountStr.isEmpty()) {
                    try {
                        tx.setAmount(new BigDecimal(amountStr));
                    } catch (Exception e) {
                        sendResponse(t, 400, "{\"hata\":\"Gecersiz amount\"}");
                        return;
                    }
                }

                transactionRepository.update(tx);
                sendResponse(t, 200, "{\"durum\":\"BASARILI\"}");
                return;
            }

            sendResponse(t, 405, "{\"hata\":\"Yontem desteklenmiyor\"}");
        }
    }

    class TransactionCreateHandler implements HttpHandler {
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
            String amountStr = extractValue(body, "amount");

            // Yeni model (transactions sayfası)
            String fullName = extractValue(body, "fullName");
            String bankCode = extractValue(body, "bankCode");
            String address = extractValue(body, "address");
            String recordDateStr = extractValue(body, "recordDate");

            boolean hasNewModel = fullName != null || bankCode != null || address != null || recordDateStr != null;
            if (hasNewModel) {
                if (fullName == null || amountStr == null || bankCode == null || address == null) {
                    sendResponse(t, 400, "{\"hata\":\"Eksik bilgiler\"}");
                    return;
                }
                BigDecimal amount;
                try {
                    amount = new BigDecimal(amountStr);
                } catch (Exception e) {
                    sendResponse(t, 400, "{\"hata\":\"Gecersiz amount\"}");
                    return;
                }
                LocalDate recordDate = null;
                if (recordDateStr != null && !recordDateStr.isEmpty()) {
                    try {
                        recordDate = LocalDate.parse(recordDateStr);
                    } catch (Exception e) {
                        sendResponse(t, 400, "{\"hata\":\"Gecersiz recordDate\"}");
                        return;
                    }
                }
                if (recordDate == null) recordDate = LocalDate.now();

                Transaction created = transactionRepository.create(new Transaction(null, fullName, amount, bankCode, address, recordDate));
                sendResponse(t, 201, SimpleJson.toJson(created));
                return;
            }

            // Eski model (geri uyumluluk)
            String userIdStr = extractValue(body, "userId");
            String desc = extractValue(body, "description");
            String status = extractValue(body, "status");
            if (userIdStr == null || amountStr == null || desc == null) {
                sendResponse(t, 400, "{\"hata\":\"Eksik bilgiler\"}");
                return;
            }
            long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                sendResponse(t, 400, "{\"hata\":\"Gecersiz userId\"}");
                return;
            }
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
            } catch (Exception e) {
                sendResponse(t, 400, "{\"hata\":\"Gecersiz amount\"}");
                return;
            }
            if (status == null || status.isEmpty()) status = "BASARILI";

            Transaction created = transactionRepository.create(new Transaction(null, userId, desc, amount, status));
            sendResponse(t, 201, SimpleJson.toJson(created));
        }
    }

    class TransferHandler implements HttpHandler {
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
            String toName = extractValue(body, "toName");
            String iban = extractValue(body, "iban");
            String amountStr = extractValue(body, "amount");
            String desc = extractValue(body, "description");
            if (iban == null || amountStr == null) {
                sendResponse(t, 400, "{\"hata\":\"Eksik bilgiler\"}");
                return;
            }
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
            } catch (Exception e) {
                sendResponse(t, 400, "{\"hata\":\"Gecersiz amount\"}");
                return;
            }

            StringBuilder d = new StringBuilder();
            d.append("Transfer");
            if (toName != null && !toName.isEmpty()) d.append("->").append(toName);
            d.append(" ").append(iban);
            if (desc != null && !desc.isEmpty()) d.append(" |").append(desc);

            Transaction created = transactionRepository.create(new Transaction(null, authed.getId(), d.toString(), amount.negate(), "BASARILI"));
            sendResponse(t, 201, SimpleJson.toJson(created));
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

    private String urlDecode(String v) {
        if (v == null) return null;
        try {
            return URLDecoder.decode(v, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return v;
        }
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
