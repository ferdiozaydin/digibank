## DigiBank – Çalıştırma ve Demo Adımları (Repo ile uyumlu)

Bu doküman, repodaki mevcut implementasyona göre sistemi ayağa kaldırma ve ana senaryoları demo etme adımlarını özetler.

### 1) Mimariyi Kısaca Anla

- **Backend (Java 17)**: Gömülü `HttpServer` üzerinde REST API (`/api/*`).
- **GUI (Flask / Python)**: Web arayüz; Java API’ye `requests` ile bağlanır.
- **DB (PostgreSQL)**: `docker-compose.yml` ile ayağa kalkar; backend `DB_URL/DB_USER/DB_PASS` verilirse `users` ve `transactions` tablolarını kullanır. Bağlantı yoksa bellek/cache + dosya fallback devreye girer.
- **Mailpit**: GUI’nin “transactions email” akışı SMTP ile mailpit’e yollar; web arayüzü üzerinden görüntülenir.
- **txt/**: Backend `/api/export` çıktısını buraya yazar; ayrıca backend içinde `DirectoryWatcherService` bu klasörü izler (Java tarafında e-posta gönderimi **konsola simüle edilir**).

### 2) Docker ile Hızlı Başlatma (Önerilen)

1. Proje kök dizininde:

```bash
docker compose up --build
```

2. Servisler:

- GUI: `http://localhost:8000`
- Backend API: `http://localhost:8080`
- Mailpit UI: `http://localhost:8025`
- Postgres: `localhost:5432` (opsiyonel olarak dış araçla bağlanılabilir)

### 3) Giriş Bilgileri (Demo)

- Varsayılan kullanıcı: `admin`
- Varsayılan şifre: `admin`
- Varsayılan TOTP: `000000`

Notlar:

- Backend tarafında admin kullanıcısı başlangıçta oluşturulur ve TOTP “DEMO\*” secret ile `000000` kabul edilir.
- GUI login ekranında da varsayılanlar aynı olacak şekilde tanımlıdır.

### 4) Demo Senaryoları (GUI üzerinden)

1. **Dashboard**: Kullanıcı bilgisi + metrik snapshot (backend açıksa canlı, değilse demo değerler).
2. **Smart Government**: `/api/bills` ile fatura listele, `/api/pay` ile FIAT/BTC/ETH/STABLE ödeme dene.
3. **Transfer**: `/api/transfer` çağrısı ile transfer işlemi kaydı oluştur.
4. **Home Control**: `/api/home/toggle` ve `/api/home/thermostat` ile ev cihazı/termostat aksiyonları.
5. **Users (Admin)**: `/api/users`, `/api/users/register`, `/api/users/item`, `/api/users/search`.
6. **Transactions (Admin)**: `/api/transactions`, `/api/transactions/create`, `/api/transactions/item`, `/api/transactions/search`.
7. **Export (Admin)**: GUI’den export tetikle; backend `txt/` içine `user_export_YYYYMMDD_HHMMSS.txt` yazar.
8. **Transactions Download/Email (GUI)**: GUI tarafında XLSX/PDF üretip indirebilir veya SMTP ile mailpit’e gönderebilirsin.

### 5) API’yi Hızlı Doğrulama (İsteğe Bağlı)

1. Token al:

```bash
curl -s -X POST http://localhost:8080/api/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin","totp":"000000"}'
```

2. Token ile kullanıcıyı çek:

```bash
curl -s http://localhost:8080/api/user -H 'Authorization: Bearer <TOKEN>'
```

### 6) Dosya İzleme (Bonus)

- Backend çalışırken `txt/` klasörüne yeni bir dosya oluşursa, Java tarafındaki `DirectoryWatcherService` bunu yakalar ve `EmailNotificationObserver` üzerinden **konsola e-posta gönderimi simülasyonu** basar.
