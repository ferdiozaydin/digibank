# DigiBank

**Kısa Açıklama**

DigiBank, bankacılık işlemlerini, ödeme entegrasyonlarını ve akıllı şehir/sürdürülebilirlik senaryolarını içeren örnek bir Java tabanlı proje ve basit bir Flask tabanlı yönetici/özellik arayüzü içerir. Proje, mikroservis/starter yapılandırmaları ve örnek desen uygulamaları (adapter, command, observer, vb.) barındırır.

---

## Gereksinimler ✅

- Docker (Desktop veya Engine) yüklü olmalı
- Docker Compose (v2 tavsiye edilir) ya da `docker compose` komutu kullanılabilir
- macOS için: Docker Desktop kurulu olmalıdır

> Not: Projenin detaylı gereksinimleri ve mimari açıklaması için `docs/0_Steps.md` ve `docs/1_requirements.md` dosyalarına bakabilirsiniz.

---

## Hızlı Başlatma 🔧

1. Proje kök dizinine gidin:

```bash
cd /path/to/digibank
```

2. Konteynerleri oluşturup çalıştırın (önerilen):

```bash
docker compose up --build
```

Arka planda çalıştırmak için:

```bash
docker compose up -d --build
```

Kapatmak için:

```bash
docker compose down
```

Konteynerlerin loglarını takip etmek için:

```bash
docker compose logs -f
```

---

## Erişim ve hizmetler 🌐

- Web GUI (Flask) varsayılan olarak `http://localhost:5000` adresinde çalışır (gui klasöründeki Dockerfile ve `app.py` tarafından kontrol edilir).
- API ve diğer servisler `docker-compose.yml` içindeki port ayarlarına göre erişilebilir.

---

## Faydalı ipuçları / Sorun giderme 💡

- Eğer port çakışması varsa, ilgili portu kullanan başka servisleri durdurun veya `docker-compose.yml` içinden port atamasını değiştirin.
- Docker kaynak limitleri (özellikle macOS Docker Desktop) performansı etkileyebilir; gerekiyorsa ayarları güncelleyin.
- Daha fazla belge için `docs/` klasörünü inceleyin.

---

## Geliştirme ve katkı 🛠️

- Geliştirme sırasında konteynerleri yeniden oluşturmak için `--build` parametresini kullanın.
- Yeni özellik eklemeleri veya hata düzeltmeleri için lütfen proje dokümantasyonuna göz atın ve commit mesajlarını açıklayıcı yazın.

---

Herhangi bir değişiklik isterseniz, README'yi projenin ihtiyaçlarına göre genişletebilirim.