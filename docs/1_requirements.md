# 1. Gereksinim Analizi (Uygulama ile birebir uyumlu)

Bu doküman, repodaki mevcut prototipin **gerçekte sunduğu** özellikleri ve sınırlarını tanımlar. Amaç; gereksinimlerin, kod (Java backend + Flask GUI) ve dağıtım (Docker Compose) ile tutarlı olmasıdır.

## 1.1 Kapsam ve Paydaşlar

**Kapsam:**

- Java 17 tabanlı gömülü HTTP API (`/api/*`) ile bankacılık + smart-city demo akışları
- Flask tabanlı GUI (`/`, `/smart-gov`, `/users`, `/transactions`, vb.)
- Docker Compose ile çalıştırma: `backend-java`, `frontend-gui`, `db (PostgreSQL)`, `mailpit`

**Aktörler:**

- **Resident**: API üzerinden / GUI üzerinden ödeme, transfer, home-control
- **Admin**: Kullanıcı/işlem yönetimi, metrik/tahmin görüntüleme, export
- **Smart Government (Simülasyon)**: Fatura üretimi (mock)
- **Mail system (Mailpit)**: GUI’nin SMTP ile e-posta göndermesi

## 1.2 Fonksiyonel Gereksinimler

### FR-01 Kimlik Doğrulama (Parola + TOTP + Backoff)

- Backend `/api/login` uç noktası ile `username/password/totp` doğrulanır.
- Parola, `SHA3-512 + per-user salt` ile saklanır; karşılaştırma sabit-zamanlıdır.
- TOTP doğrulaması vardır; **DEMO secret** için `000000` kabul edilebilir.
- Başarısız denemelerde artan gecikme ve kısa süreli kilitleme uygulanır.

### FR-02 Token Tabanlı Yetkilendirme ve Rol Kontrolü

- Başarılı login sonrası bearer token üretilir ve isteklerde `Authorization: Bearer <token>` ile taşınır.
- Rol kontrolü uygulanır:
  - **ADMIN**: `/api/users*`, `/api/transactions*` üzerinde CRUD; `/api/export`, `/api/metrics`, `/api/forecast`
  - **RESIDENT/ADMIN**: `/api/user`, `/api/bills`, `/api/pay`, `/api/transfer`, `/api/home/*`

### FR-03 Kullanıcı Görüntüleme

- `/api/user` ile oturumdaki kullanıcının temel bilgisi ve bakiyeleri döner.

### FR-04 Smart Government Fatura Listeleme

- `/api/bills` ile mock/simülasyon faturalar listelenir.
- Faturalar `billingId/type/amount/date/isPaid` alanlarını taşır.

### FR-05 Ödeme (FIAT veya Kripto Adapter)

- `/api/pay` ile fatura ödemesi yapılır.
- `payMode` alanı `FIAT|BTC|ETH|STABLE` değerlerini destekler.
- FIAT için `FiatPaymentStrategy`; kripto için `BtcAdapter/EthAdapter/StablecoinAdapter` kullanılır.
- Sonuç `TransactionRepository`’ye transaction kaydı olarak işlenir.

### FR-06 Transfer Kaydı

- `/api/transfer` ile transfer işlemi kaydı oluşturulur (tutar negatif kaydedilir).

### FR-07 Kullanıcı Yönetimi (Admin)

- Liste: `/api/users` (GET)
- Arama: `/api/users/search?q=` (GET)
- Ekleme: `/api/users/register` (POST)
- Tekil işlem: `/api/users/item?username=` (GET/PUT/DELETE)

### FR-08 İşlem Yönetimi (Admin + Kısmi Resident Görünürlüğü)

- Liste: `/api/transactions` (GET)
  - ADMIN: tüm işlemler
  - RESIDENT: sadece kendi işlemleri
- Arama: `/api/transactions/search?q=` (GET) aynı rol mantığı
- Ekleme: `/api/transactions/create` (POST) yalnız ADMIN
  - Yeni “manuel işlem” modeli: `fullName, amount, bankCode, address, recordDate`
  - Eski model (geri uyumluluk): `userId, description, amount, status`
- Tekil işlem: `/api/transactions/item?id=` (GET/PUT/DELETE)
  - PUT/DELETE yalnız ADMIN

### FR-09 Export (Admin)

- `/api/export` ile `txt/` altına `user_export_YYYYMMDD_HHmmss.txt` yazılır.
- Not: Export içeriği demo amaçlıdır; bazı satırlar sabit örnek veri içerebilir.

### FR-10 Metrikler ve Tahmin (Admin)

- `/api/metrics` ile demo metrik snapshot
- `/api/forecast?type=traffic|energy` ile demo tahmin çıktısı

### FR-11 Home Automation (Resident/Admin)

- `/api/home/toggle` ile cihaz aç/kapat
- `/api/home/thermostat` ile termostat hedef sıcaklık ayarı

### FR-12 GUI (Flask) Fonksiyonları

GUI; backend API’yi çağırarak aşağıdaki ekranları sağlar:

- Login (`/login`) → backend `/api/login`
- Dashboard (`/`) → `/api/user`, `/api/metrics`
- Smart Government (`/smart-gov`) → `/api/bills`, `/api/pay`
- Transfer (`/transfer`) → `/api/transfer`
- Users (`/users`) → `/api/users*`
- Transactions (`/transactions`) → `/api/transactions*`
- Transactions çıktıları: XLSX/PDF indirme ve SMTP ile e-posta (Mailpit)

## 1.3 Veri ve Kalıcılık

**PostgreSQL aktifse** (Docker Compose ile varsayılan):

- `users` ve `transactions` tabloları oluşturulur ve kullanılır.

**DB yoksa/fail ederse**:

- `UserRepository`: cache (in-memory)
- `TransactionRepository`: in-memory store + dosyaya append (örn. `data/transactions.log`)

## 1.4 Güvenlik Gereksinimleri (Prototip Seviyesi)

- SHA3-512 + salt parola saklama
- TOTP doğrulama (demo bypass koşullu)
- Basit bearer token (in-memory token store)
- İsteğe bağlı HTTPS zorunluluğu: `REQUIRE_HTTPS=true` ise `X-Forwarded-Proto: https` beklenir
- Geliştirici bypass token: `DEV_AUTH_TOKEN` ayarlıysa belirli token ile auth bypass (demo kolaylığı)

## 1.5 Kapsam Dışı / Bilinçli Basitleştirmeler

- Gerçek banka entegrasyonu veya gerçek e-devlet entegrasyonu yok (tamamı simülasyon)
- Token’ların kalıcılığı yok (backend restart’ta tokenlar sıfırlanır)
- Rate-limit, gerçek TLS terminasyonu, kapsamlı denetim kaydı (persisted audit) üretim kapsamındadır
