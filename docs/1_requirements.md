# 1. Gereksinim Analizi

Sistem; şehir sakinlerinin park ücreti, toplu taşıma bileti, fatura ve benzeri şehir hizmetlerini hem **geleneksel para birimi** (fiat) hem de **kripto para (BTC/ETH/Stablecoin simülasyonu)** ile ödeyebilmesini sağlamalıdır. Kullanıcılar tek bir arayüz üzerinden MFA ile güvenli giriş yapabilmeli, fatura listesini görebilmeli, ödeme başlatabilmeli ve her işlem sonrası izlenebilirlik sağlayan kayıtlar üretilmelidir. CityController ve sensör akışı, otomasyon senaryoları sonucu oluşan olayları ve ücretlendirmeleri (örneğin trafik ışığı ayarı, güvenlik uyarısı) bankacılık/şehir servislerine iletmelidir.

Teknik açıdan DigiBank prototipi **tek JVM içinde gömülü HttpServer** olarak çalışır; kullanıcı ve işlem verileri bellek içi repository’lerde tutulur, TXT ihracat ile dışa aktarılır. Sistem; SHA3-512 + salt parola saklama, TOTP tabanlı MFA, basit bearer token üretimi, AuditLogger ile iz kaydı ve Observer/Command/Strategy/Adapter tasarım kalıplarıyla güçlendirilmiştir. Gerçek banka veya zincir entegrasyonu yoktur; kripto ödemeler adapterlar ile simüle edilir. Dağıtım için tek konteyner (Dockerfile) öngörülür; TLS, oran sınırlama ve kalıcı veritabanı üretim adımında eklenecek konulardır.

## **Fonksiyonel Gereksinimler (Functional Requirements)**

### **FR-01 Güvenli Kullanıcı Girişi (MFA)**

Kullanıcılar SHA3-512 + salt ile saklanan parola ve TOTP kodu ile giriş yapmalı; başarısız denemelerde kademeli bekleme ve geçici kilit uygulanmalıdır. Başarılı giriş sonrası bearer token üretilmelidir.

### **FR-02 Fatura Görüntüleme ve Ödeme (Fiat/Kripto Simülasyonu)**

Kullanıcılar `/api/bills` ile faturaları görüp `/api/pay` ile FIAT veya BTC/ETH/Stablecoin seçenekleriyle ödeme yapabilmelidir; sonuçlar TransactionRepository ve AuditLogger’a yazılmalıdır.

### **FR-03 Şehir Otomasyon Rutinleri ve Komutları**

CityController günlük rutinleri (LightingRoutine, SecuritySweepRoutine) çalıştırmalı, komut kuyruğunda trafik/aydınlatma/ev cihazı komutlarını CommandInvoker ile işletebilmelidir.

### **FR-04 Sensör Olay Yönetimi ve Bildirim**

SensorSystem trafik/güvenlik/yangın olaylarını üretmeli, EmergencyService/PublicUtilityService/BankingNotificationService gözlemcilerine iletmeli ve gerekli aksiyonları tetiklemelidir.

### **FR-05 Audit ve İhracat**

Tüm kimlik doğrulama ve ödeme işlemleri AuditLogger ile kaydedilmeli; `/api/export` ile kullanıcı/işlem listesi TXT olarak dışa aktarılmalıdır.

### **FR-06 Yönetici Gözlemi**

Yönetici `/api/metrics` ve `/api/forecast` uçlarıyla örnek metrik ve tahmin verilerini alabilmelidir.

---

### FR-07 Yönetici Paneli (Admin Dashboard)

Yöneticiler `/api/users` ve `/api/transactions` uçları üzerinden tüm kullanıcı ve işlemleri listeleyebilmeli, arama yapabilmeli (`/search`), yeni kayıt oluşturabilmeli (Register/Create) ve mevcut kayıtları düzenleyip silebilir (`/item`). Ayrıca `/api/transfer` ile yöneticiler (veya kullanıcılar) doğrudan para transferi yapabilmelidir.

---

## **Fonksiyonel Olmayan Gereksinimler (Non-Functional Requirements)**

### **NFR-01 Dağıtım ve Çalıştırma**

Sistem tek JVM içinde gömülü HttpServer ile çalışmalı; Docker konteynerında ek bağımlılık olmadan ayağa kalkabilmelidir.

### **NFR-02 Veri Saklama**

Veri bellek içi repository’lerde tutulur; kalıcılık gereksinimi TXT ihracat ile karşılanır. Harici veritabanı veya mesaj kuyruğu yoktur.

### **NFR-03 Performans (Demo)**

Demo koşullarında düşük gecikme (<300ms hedef) beklenir; yük testi ve yatay ölçekleme kapsam dışıdır.

### **NFR-04 Modüler Mimari ve Kalıplar**

Kod tabanı Strategy, Adapter, Observer, Command, Template Method ve Singleton kalıplarını kullanarak genişletilebilirlik sağlar.

---

## **4.3 Güvenlik Gereksinimleri (Security Requirements)**

### **SEC-01 Parola Saklama ve Doğrulama**

Parolalar SHA3-512 + kullanıcıya özgü salt ile saklanmalı, sabit zamanlı karşılaştırma yapılmalıdır.

### **SEC-02 TOTP Çok Faktörlü Doğrulama**

Girişlerde TOTP doğrulaması uygulanmalı; demo modunda yalnızca "DEMO" gizli anahtar + 000000 koduna izin verilmelidir.

### **SEC-03 Hesap Koruması**

Başarısız girişlerde kademeli bekleme ve geçici kilitleme uygulanmalıdır.

---

## **4.4 Harici Entegrasyon Gereksinimleri (External Interface Requirements)**

### **EXT-01 Kripto Adaptörleri (Simülasyon)**

BTC/ETH/Stablecoin ödemeleri gerçek ağ bağlantısı olmadan adapter katmanında simüle edilmelidir.

### **EXT-02 Kamu Hizmetleri Bildirimleri**

Sensör kaynaklı olaylar EmergencyService ve PublicUtilityService gözlemcilerine iletilmelidir.

---

## **4.5 Özet Gereksinim Tablosu**

| Kategori            | Kod    | Gereksinim                                       |
| ------------------- | ------ | ------------------------------------------------ |
| Fonksiyonel         | FR-01  | MFA (parola + TOTP) ile giriş, token üretimi     |
| Fonksiyonel         | FR-02  | Fatura listeleme ve FIAT/BTC/ETH/Stable ödeme    |
| Fonksiyonel         | FR-03  | TransactionRepository + AuditLogger kaydı        |
| Fonksiyonel         | FR-04  | CityController rutinleri ve komut kuyruğu        |
| Fonksiyonel         | FR-05  | SensorSystem olay bildirimi (acil/kamu/bildirim) |
| Fonksiyonel         | FR-06  | TXT ihracat ve metrik/forecast uçları            |
| Fonksiyonel         | FR-07  | Admin Dashboard (Kullanıcı/İşlem CRUD, Transfer) |
| Fonksiyonel Olmayan | NFR-01 | Tek JVM + Docker ile hafif kurulum               |
| Fonksiyonel Olmayan | NFR-02 | Bellek içi saklama, TXT ihracatı                 |
| Fonksiyonel Olmayan | NFR-03 | Demo performans (<300ms hedef)                   |
| Fonksiyonel Olmayan | NFR-04 | Modüler tasarım kalıpları                        |
| Güvenlik            | SEC-01 | SHA3-512 + salt parola saklama                   |
| Güvenlik            | SEC-02 | TOTP MFA ve kilitleme/backoff                    |
| Harici Entegrasyon  | EXT-01 | Kripto adapter simülasyonu                       |
| Harici Entegrasyon  | EXT-02 | Acil/kamu gözlemci bildirimi                     |

---
