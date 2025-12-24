# 3. Sözde Kod (Java Tabanlı Pseudocode)

Bu doküman, depoda yer alan Java prototipinin çekirdek akışlarını özetler.

## 3.1. Kullanıcı Giriş ve Kimlik Doğrulama (TOTP + Salted Hash)

**Senaryo:** Kullanıcı, SHA3-512 + salt ile doğrulanır; TOTP ve backoff uygulanır.

```java
public class AuthenticationService {
    public boolean login(User user, String password, String totp) {
        LoginAttempt attempt = attempts.computeIfAbsent(user.getUsername(), u -> new LoginAttempt());
        if (attempt.isLocked()) return false; // çoklu deneme kilidi

        boolean passwordOk = verifyPassword(password, user.getSalt(), user.getPasswordHash());
        boolean totpOk = verifyTotp(user.getTotpSecret(), totp); // demo ortamı: secret "DEMO*" ise 000000 kabul

        if (passwordOk && totpOk) {
            attempt.reset();
            AuditLogger.getInstance().log("Kullanici giris yapti: " + user.getUsername());
            return true;
        }

        attempt.registerFailure();
        Thread.sleep(attempt.backoffMillis()); // artan gecikme
        AuditLogger.getInstance().log("Giris basarisiz: " + user.getUsername());
        return false;
    }
}
```

## 3.2. Hibrit Ödeme Motoru (Strategy + Adapter)

**Senaryo:** Fiat ödemeleri `PaymentStrategy`, kripto ödemeleri `CryptoAdapter` ile işlenir ve `TransactionRepository` kaydı düşülür.

```java
public class PaymentService {
    public boolean processPayment(User user, BigDecimal amount, PaymentStrategy strategy) {
        AuditLogger.getInstance().log("Odeme baslatildi: " + user.getUsername());
        boolean success = strategy.pay(user, amount);
        String durum = success ? "BASARILI" : "BASARISIZ";
        repository.save(new Transaction(now(), user.getId(), "Odeme", amount, durum));
        if (!success) AuditLogger.getInstance().log("Odeme tamamlanamadi");
        return success;
    }

    public boolean processCryptoPayment(User user, BigDecimal amountFiat, CryptoAdapter adapter) {
        AuditLogger.getInstance().log("Kripto odeme: " + adapter.name());
        boolean success = adapter.pay(user, amountFiat);
        repository.save(new Transaction(now(), user.getId(), "Kripto-" + adapter.name(), amountFiat, success ? "BASARILI" : "BASARISIZ"));
        return success;
    }
}
```

## 3.3. CityController Otomasyon ve Komut Kuyruğu

**Senaryo:** Günlük rutinler, sensör olayları ve komut kuyruğu tek noktadan yürütülür.

```java
public class CityController {
    public void runDailyRoutines() {
        new LightingRoutine(infrastructureController, "Merkez Cadde").runRoutine();
        new SecuritySweepRoutine(sensorSystem).runRoutine();
    }

    public void simulateCityEvents() {
        sensorSystem.triggerEvent("TRAFFIC_JAM", "Broadway Ave");
        sensorSystem.triggerEvent("FIRE", "Downtown 5th St");
        sensorSystem.triggerEvent("WATER_LEAK", "Rezidans A Blok");
    }

    public void executeQueuedCommands() { commandInvoker.runAll(); }

    public void demoHomeAutomation() {
        homeDeviceController.toggleDevice("LIGHT-LIVINGROOM", true);
        homeDeviceController.setThermostat("Salon", 22.5);
    }
}
```

## 3.4. Sensör Olay Bildirimi (Observer Pattern)

**Senaryo:** `SensorSystem` olayları gözlemcilere (BankingNotificationService, EmergencyService, PublicUtilityService) yayınlar.

```java
public class SensorSystem {
    private List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer o) { observers.add(o); }
    public void removeObserver(Observer o) { observers.remove(o); }

    public void triggerEvent(String eventType, String location) {
        AuditLogger.getInstance().log("Event: " + eventType + " loc=" + location);
        for (Observer o : observers) { o.update(eventType, location); }
    }
}

public class EmergencyService implements Observer {
    public void update(String eventType, String location) {
        if ("FIRE".equals(eventType)) dispatchTeam(location);
    }
}

public class BankingNotificationService implements Observer {
    public void update(String eventType, String location) {
        // Bölgedeki kullanıcılara push
    }
}
```

## 3.5. Yönetici (Admin) İşlemleri

**Senaryo:** Yöneticiler, `ApiServer` üzerindeki endpoint'leri kullanarak kullanıcıları ve işlemleri yönetir (`TransactionCreateHandler`, `UserItemHandler` vb.).

```java
public class UserItemHandler {
   public void handle(Exchange t) {
       if ("PUT".equals(t.getMethod())) {
           // Kullanıcı rolü veya bakiyesi güncelle
           userRepository.upsert(updatedUser);
       } else if ("DELETE".equals(t.getMethod())) {
           userRepository.deleteByUsername(username);
       }
   }
}
```
