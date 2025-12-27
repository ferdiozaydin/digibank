# DigiBank & Smart City Entegrasyonu – Kapsamlı Proje Raporu

**Öğrenci:** Ferdi Özaydın / 2420003042
**Ders:** Nesne Tabanlı Programlama
**Tarih:** 27.12.2025

## 1. Yönetici Özeti

Bu proje, “dijital bankacılık” akışlarını “smart city otomasyonu” ile birleştiren çalışır bir prototip sunar. Sistem iki ana parçadan oluşur:

- **Java 17 Backend**: Gömülü `HttpServer` ile REST API sağlar; MFA login, ödeme, transfer, admin CRUD, export, metrik/tahmin ve home-control uçları içerir.
- **Python/Flask GUI**: Web arayüz; Java API’yi tüketir; admin ekranları ve raporlama (XLSX/PDF, e-posta) gibi ek demo fonksiyonları sağlar.

Dağıtım, `docker-compose.yml` ile **PostgreSQL** ve **Mailpit** servislerini de içerir. Backend, DB ortam değişkenleri varsa `users/transactions` tablolarını kullanır; DB yoksa in-memory + dosya fallback mantığına döner.

## 2. Projenin Amacı ve Kapsamı

Amaç; aşağıdaki konuları tek bir repo üzerinde göstermek:

- MFA ve rol bazlı erişim kontrolü
- FIAT + simüle kripto ödeme (Strategy/Adapter)
- Admin yönetimi (Users/Transactions CRUD)
- Operasyonel çıktılar: export, raporlama, e-posta demo
- Smart-city olay akışı: Observer/Command/Template Method

Kapsam dışı: gerçek e-devlet entegrasyonu, üretim-grade token yönetimi, tam TLS terminasyonu ve yük testi.

## 3. Sistem Bileşenleri

### 3.1 Backend (Java)

Backend’in giriş noktası `Main` sınıfıdır:

- Admin kullanıcı oluşturur (demo)
- `DirectoryWatcherService("txt")` ile export klasörünü izler
- Demo komutlarını kuyruğa ekler
- `ApiServer`’ı başlatır (varsayılan 8080)

API uçları (özet):

- Auth: `POST /api/login`
- User: `GET /api/user`
- Bills/Pay: `GET /api/bills`, `POST /api/pay`
- Transfer: `POST /api/transfer`
- Admin Users: `/api/users`, `/api/users/register`, `/api/users/search`, `/api/users/item`
- Admin Transactions: `/api/transactions`, `/api/transactions/create`, `/api/transactions/search`, `/api/transactions/item`
- Ops: `POST /api/export`, `GET /api/metrics`, `GET /api/forecast`
- Home: `POST /api/home/toggle`, `POST /api/home/thermostat`

### 3.2 GUI (Flask)

GUI; backend’e token ile bağlanır ve aşağıdaki ekranları sağlar:

- Dashboard + Performance
- Smart Government (fatura ödeme)
- Transfer
- Users (ADMIN)
- Transactions (ADMIN) + XLSX/PDF indirme + SMTP ile e-posta
- Home Control

### 3.3 Postgres (Opsiyonel Kalıcılık)

Backend; `DB_URL/DB_USER/DB_PASS` set edilmişse:

- `users` tablosu (id, username, password_hash, salt, totp_secret, role, fiat_balance, crypto_balance, pq_public_key)
- `transactions` tablosu (id, user_id, description, amount, status, created_at, full_name, bank_code, address, record_date)

DB yoksa:

- Users cache’de tutulur
- Transactions hem listede tutulur hem de dosyaya append edilebilir

### 3.4 Mailpit

GUI’nin `/transactions/email` akışı SMTP ile mail gönderir; Mailpit bu mailleri yakalar ve web UI üzerinden gösterir.

## 4. Tasarım Kalıpları

- **Singleton**: `AuditLogger`
- **Strategy**: `PaymentStrategy` (FIAT/crypto balance stratejisi)
- **Adapter**: `CryptoAdapter` (BTC/ETH/STABLE simülasyonu)
- **Observer**: `SensorSystem`, `DirectoryWatcherService`
- **Command**: `CommandInvoker` ve altyapı/home komutları
- **Template Method**: Günlük rutin şablonu ve alt sınıflar

## 5. Güvenlik

Prototip seviyesinde uygulanmış güvenlik unsurları:

- SHA3-512 + salt parola saklama
- TOTP doğrulama (DEMO secret için `000000` kabul edilebilir)
- Backoff ve geçici kilitleme
- Rol bazlı endpoint yetkilendirme
- Opsiyonel HTTPS zorunluluğu (`REQUIRE_HTTPS=true` ve `X-Forwarded-Proto`)
- Demo kolaylığı için opsiyonel auth bypass (`DEV_AUTH_TOKEN`)

## 6. Dağıtım ve Çalıştırma

Önerilen yöntem:

```bash
docker compose up --build
```

Erişim:

- GUI: `http://localhost:8000`
- Backend: `http://localhost:8080`
- Mailpit: `http://localhost:8025`

## 7. Test Stratejisi (Prototip)

- **E2E manuel test**: GUI üzerinden login → bills/pay → transfer → users/transactions CRUD
- **API smoke test**: `curl` ile `/api/login` ve token ile `/api/user`
- **Export/Watcher**: export tetiklenip `txt/` altında dosya oluştuğu doğrulanır; watcher konsola simülasyon basar
- **Mail**: GUI transactions email ile mailpit’te mail görüldüğü doğrulanır

## 8. Sonuç ve Gelecek Çalışmalar

Bu repo; OOP tasarım kalıpları, güvenlik temelleri ve operasyonel bileşenlerle (DB/mail/export) desteklenmiş küçük ama çalışır bir prototip ortaya koyar.

Gelecek adımlar:

- Token yaşam döngüsü, refresh, rate limiting
- Persisted audit log ve izlenebilirlik (correlation ID)
- Smart Government faturalarının gerçek servis/mock server ile ayrıştırılması
- Olay akışının message bus ile gerçekçi hale getirilmesi

## 9. Ekler ve İlgili Dokümanlar

- Çalıştırma adımları: `docs/0_Steps.md`
- Gereksinimler: `docs/1_requirements.md` ve `docs/1_requirements_ieee.md`
- Diyagramlar: `docs/2_uml_c4.mmd`, `docs/2_uml_class.mmd`, `docs/2_uml_usecase.mmd`
- Psödokod: `docs/3_pseudocode.md`
- DB şeması: `docs/ek_a_database_schema.mmd`
- Proje yapı diyagramı: `docs/ek_c_source_structure.mmd`
