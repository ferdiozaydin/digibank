# Araştırma Özeti

Bu repo, **Java backend + Python/Flask GUI** birleşimi üzerinden bir “smart city + dijital bankacılık” prototipini OOP tasarım kalıplarıyla ilişkilendirerek aşağıdaki gözlemleri sunar:

- **Strategy + Adapter (Ödeme esnekliği):** `PaymentService` FIAT ödemelerinde `PaymentStrategy` (`FiatPaymentStrategy`, `CryptoPaymentStrategy`) ve fatura kripto ödemelerinde `CryptoAdapter` (BTC/ETH/Stable) kullanarak ödeme kanalını değiştirilebilir kılar.
- **Observer (Olay yayını):** `SensorSystem` şehir olaylarını `EmergencyService`, `PublicUtilityService`, `BankingNotificationService` gibi dinleyicilere yayınlar. Ayrıca dosya tabanlı bir olay kaynağı olarak `DirectoryWatcherService` → `EmailNotificationObserver` zinciri, “yeni export dosyası oluştu” olayını simüle eder.
- **Command + Template Method (Otomasyon):** `CommandInvoker` altyapı/ev otomasyonu komutlarını sıraya alır; `DailyRoutineTemplate` tabanlı `LightingRoutine` ve `SecuritySweepRoutine` çalıştırılır.
- **Güvenlik yaklaşımı (prototip seviyesi):** `AuthenticationService` SHA3-512 + salt, TOTP doğrulama, artan gecikme ve geçici kilitleme uygular. `ApiServer` token store’u in-memory tutar (restart’ta sıfırlanır). `REQUIRE_HTTPS` ve `DEV_AUTH_TOKEN` gibi env ayarları demo/ortam varyasyonlarını gösterir.
- **Kalıcılık ve çevre bağımlılıkları:** `UserRepository` ve `TransactionRepository`, Postgres ortam değişkenleri verildiğinde DB’ye bağlanır; değilse cache/in-memory + dosya fallback yaklaşımına döner. Bu, prototiplerde sık görülen “opsiyonel kalıcılık” desenini gösterir.
- **Sistem entegrasyonu (polyglot):** Flask GUI, Java API’ye HTTP ile bağlanır; ayrıca e-posta senaryosu için SMTP üzerinden Mailpit kullanır. Bu, “tek kod tabanı = tek dil” varsayımının her zaman doğru olmadığını pratikte gösterir.

Gelecek çalışmalar için araştırma eksenleri:

1. Oturum yönetimi (token yaşam süresi, refresh, CSRF), rate limit ve TLS terminasyonu.
2. Audit log’ların kalıcı veri modeli (DB’ye yazma, korelasyon ID’leri).
3. Olay akışının mesaj kuyruğu ile ayrıştırılması (Observer yerine event bus) ve “en az bir kez” teslim semantiği.
4. Ödeme stratejilerinde gerçek kur/veri beslemesi ve uçtan uca test otomasyonu.
