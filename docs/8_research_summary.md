# 8. Araştırma Özeti

Repo içindeki çalışan Java prototipi, OOP prensiplerini somut tasarım kalıplarıyla birleştirerek aşağıdaki gözlemleri sunmaktadır:

- **Strategy + Adapter:** `PaymentService`, fiat için `PaymentStrategy`, kripto için `CryptoAdapter` (BTC/ETH/Stable) kullanarak ödeme kanalını esnekleştiriyor.
- **Observer:** `SensorSystem` acil durum, altyapı arızası ve bankacılık bildirimlerini `EmergencyService`, `PublicUtilityService`, `BankingNotificationService` gibi dinleyicilere yayınlıyor.
- **Command + Template Method:** `CommandInvoker` trafik ışığı/ev otomasyonu komutlarını sıraya alıyor; `DailyRoutineTemplate` bazında `LightingRoutine` ve `SecuritySweepRoutine` çalıştırılıyor.
- **Güvenlik uygulaması:** `AuthenticationService` SHA3-512 + salt, TOTP ve geçici hesap kilidi ile minimum, fakat gerçekçi bir MFA akışı sağlıyor; `AuditLogger` (Singleton) tüm kritik olayları logluyor.

Gelecek çalışmalar: (i) bu modüler yapının mikroservis sınırlarına taşınması, (ii) PQ kriptografi alanının (şimdiki placeholder anahtar) gerçek kütüphanelerle bütünleştirilmesi, (iii) sensör olaylarında ML tabanlı anomali tespiti ve (iv) ödeme stratejilerinde gerçek oran beslemeleriyle stres testi.
