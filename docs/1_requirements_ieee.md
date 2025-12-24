# DigiBank – SRS (IEEE tarzı özet, prototip ile uyumlu)

## 1. Introduction

### 1.1 Purpose

Bu SRS, DigiBank prototipinin gereksinimlerini **uygulanmış kod** ile tutarlı şekilde tanımlar. Prototip; Java backend API, Flask GUI, Postgres (opsiyonel kalıcılık) ve Mailpit (SMTP yakalayıcı) bileşenlerinden oluşur.

### 1.2 Scope

- Gömülü Java `HttpServer` ile REST API
- MFA login (parola hash + TOTP), rol tabanlı erişim
- Smart Government fatura listeleme/ödeme (simülasyon)
- Admin kullanıcı/işlem yönetimi, export
- GUI üzerinden ödeme/transfer/users/transactions akışları

### 1.3 Definitions

- **FIAT**: Kullanıcının TL benzeri bakiyesi (`fiatBalance`)
- **Crypto**: Kullanıcının kripto bakiyesi (`cryptoBalance`), adaptör/strateji ile simüle edilir
- **MFA**: Parola + TOTP doğrulaması

## 2. Overall Description

### 2.1 Product Perspective

Sistem bir eğitim/prototip uygulamasıdır. Üretim-grade kimlik/oturum yönetimi, gerçek entegrasyonlar ve kapsamlı denetim kayıtları kapsam dışıdır.

### 2.2 Product Functions (Implemented)

- `/api/login` ile token üretimi
- `/api/user` ile kullanıcı bilgisi
- `/api/bills` + `/api/pay` ile fatura ödeme
- `/api/transfer` ile transfer kaydı
- `/api/users*` (ADMIN) ile kullanıcı CRUD
- `/api/transactions*` (ADMIN) ile işlem CRUD
- `/api/export` (ADMIN) ile `txt/` altına export dosyası
- `/api/metrics`, `/api/forecast` (ADMIN) ile demo metrik/tahmin
- `/api/home/toggle`, `/api/home/thermostat` ile home otomasyon
- Flask GUI ekranları ile bu akışların web üzerinden gösterimi

### 2.3 User Classes

- **Resident**: Ödeme/transfer/home-control
- **Admin**: Resident yetenekleri + kullanıcı/işlem yönetimi + export/metrics/forecast

### 2.4 Operating Environment

- Docker Compose ile: Java 17, Python 3.9, PostgreSQL 14, Mailpit
- Lokal çalıştırmada da backend/GUI bağımsız başlatılabilir

### 2.5 Constraints

- Token store in-memory (backend restart’ta tokenlar kaybolur)
- Smart Government faturaları simülasyondur
- Java tarafında e-posta gönderimi **simülasyon**; GUI tarafında SMTP ile Mailpit’e gönderim vardır

## 3. Specific Requirements

### 3.1 Functional Requirements

- **FR-01** MFA login: SHA3-512 + salt + TOTP, backoff/lock
- **FR-02** Token ile erişim: `Authorization: Bearer <token>`
- **FR-03** Rol bazlı yetki: ADMIN vs RESIDENT
- **FR-04** Fatura listeleme/ödeme: FIAT veya BTC/ETH/STABLE
- **FR-05** Transfer: kayıt oluşturma
- **FR-06** Admin Users CRUD
- **FR-07** Admin Transactions CRUD (manuel işlem modeli dahil)
- **FR-08** Export: `txt/` altına zaman damgalı dosya
- **FR-09** Metrik/Tahmin: demo çıktılar
- **FR-10** Home-control: toggle ve thermostat

### 3.2 External Interface Requirements

- **HTTP API**: `/api/*` JSON payload/response
- **SMTP**: GUI, Mailpit’e SMTP ile mail gönderir
- **DB**: Postgres aktifse `users` ve `transactions` tablosu

## 4. Non-Functional Requirements

### 4.1 Performance (Prototype)

- Tek instance/demo kullanım hedeflenir; resmi yük test hedefi yoktur.

### 4.2 Security (Prototype)

- SHA3-512 + salt parola saklama
- TOTP doğrulama (demo bypass koşullu)
- İsteğe bağlı HTTPS zorunluluğu (reverse-proxy arkasında)
- Developer bypass token (sadece demo ortamı)

### 4.3 Maintainability

- Strategy/Adapter/Observer/Command/Template/Singleton desenleri ile modülerlik

## 5. Traceability (Kod ile eşleme)

- API uçları: `src/com/digibank/api/ApiServer.java`
- Kimlik: `src/com/digibank/service/AuthenticationService.java`
- Ödeme: `src/com/digibank/service/PaymentService.java`
- DB katmanı: `src/com/digibank/service/UserRepository.java`, `src/com/digibank/service/TransactionRepository.java`
- GUI: `gui/app.py`
- Dağıtım: `docker-compose.yml`
