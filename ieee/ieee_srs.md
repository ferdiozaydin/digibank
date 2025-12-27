# 1. Gereksinim Analizi (IEEE SRS)

Aşağıda **DigiBank Modernizasyon Projesi** için **IEEE 830-1998 SRS standardına uygun** olarak hazırlanmış yazılım gereksinim analizi raporu yer almaktadır. Bu içerik, `digibank.exe` masaüstü uygulamasından web tabanlı, servis odaklı bir mimariye geçişi belgeler.

---

# **1. Giriş (Introduction)**

Bu dokümanın amacı, **DigiBank** bankacılık platformunun modernize edilmiş versiyonunun yazılım gereksinimlerini tanımlamaktır. Proje, eski bir masaüstü uygulamasını (`digibank.exe`) alıp, onu modern **Java Spring Boot (benzeri)** bir backend ve **Python Flask** tabanlı bir frontend mimarisine taşımayı hedefler. Yenilenen sistem; finansal işlemleri (havale, EFT, fatura), akıllı şehir entegrasyonlarını (IoT cihaz kontrolü) ve yönetimsel raporlamayı tek bir hibrit-bulut hazır yapıda sunar.

Kapsam dahilindeki başlıca özellikler:
*   Kullanıcıların güvenli erişimi (MFA/TOTP).
*   Fiat ve Kripto para birimleri ile ödeme yapabilme.
*   Akıllı şehir faturalarının (Smart Government) görüntülenip ödenebilmesi.
*   Ev otomasyonu (IoT) cihazlarının bankacılık arayüzünden yönetilebilmesi.

---

# **2. Sistem Tanımı (System Description)**

Modern DigiBank, **Servis Odaklı Mimari (SOA)** prensiplerine göre tasarlanmış, **Docker** konteynerleri üzerinde çalışan dağıtık bir sistemdir. Kullanıcılar sisteme web tarayıcıları üzerinden erişir. Arka planda Java tabanlı bir REST API, tüm iş mantığını (Business Logic) yürütürken; veri bütünlüğü PostgreSQL veritabanı ile sağlanır.

Sistem bileşenleri:
1.  **Backend API (Java 17):** Kimlik doğrulama, hesap yönetimi ve işlem motorunu barındıran çekirdek servis.
2.  **Frontend GUI (Python Flask):** Kullanıcı arayüzünü sunan ve API ile haberleşen web sunucusu.
3.  **Veritabanı (PostgreSQL):** Kullanıcı ve işlem kayıtlarının tutulduğu ilişkisel veritabanı.
4.  **Mail Sunucusu (Mailpit):** Sistem bildirimlerinin simülasyonu için yerel SMTP sunucusu.

---

# **3. Genel Bakış (Overall Description)**

## **3.1 Kullanıcı Profilleri**

*   **Sakin (Resident):** Bakiye sorgular, para transferi yapar, fatura öder ve evindeki akıllı cihazları (termostat, ışık vb.) yönetir.
*   **Yönetici (Admin):** Kullanıcı ekler/siler, sistem metriklerini izler, işlem loglarını dışa aktarır (export) ve şüpheli işlemleri denetler.
*   **Sistem (System):** Otomatik faiz işletimi, bekleyen faturaların oluşturulması gibi zamanlanmış görevleri yürütür.

## **3.2 Varsayımlar ve Bağımlılıklar**
*   Sistemin çalışması için Docker ve Docker Compose ortamının kurulu olması gereklidir.
*   E-posta gönderimi için yerel bir SMTP sunucusu (Mailpit) yeterlidir; gerçek bir ISP bağlantısı gerekmez.
*   Kripto para işlemleri "simülasyon" modunda çalışır; gerçek blokzincir ağına bağlanmaz.
*   Akıllı ev cihazları (IoT) sanal birer nesne olarak veritabanında temsil edilir.

---

# **4. Fonksiyonel Gereksinimler (Functional Requirements)**

## **4.1 Kimlik Doğrulama ve Güvenlik**
*   **FR-01 (MFA):** Sistem, kullanıcı girişinde kullanıcı adı/şifreye ek olarak Zamana Dayalı Tek Seferlik Şifre (TOTP) istemelidir.
*   **FR-02 (Hashing):** Kullanıcı şifreleri veritabanında düz metin olarak değil, `SHA3-512` ve `Salt` kullanılarak saklanmalıdır.
*   **FR-03 (Session):** Başarılı giriş sonrası API istekleri için `Bearer Token` kullanılmalıdır.

## **4.2 Hesap ve İşlem Yönetimi**
*   **FR-04 (Bakiye):** Kullanıcılar anlık bakiyelerini tek bir dashboard üzerinde görmelidir.
*   **FR-05 (Transfer):** Sistem, dahili hesaplar arası para transferine izin vermelidir.
*   **FR-06 (Ödeme Adaptörü):** Ödeme modülü, "Adapter Pattern" kullanarak hem Fiat (TL/USD) hem de Kripto (BTC/ETH) ödemelerini desteklemelidir.

## **4.3 Akıllı Şehir Entegrasyonu**
*   **FR-07 (Fatura Ödeme):** Kullanıcılar, simüle edilmiş şehir hizmetleri (Su, Elektrik) faturalarını sistem üzerinden ödeyebilmelidir.
*   **FR-08 (IoT Kontrol):** Kullanıcılar, `Aç/Kapat` komutları ile sisteme bağlı sanal cihazları yönetebilmelidir.

## **4.4 Yönetim ve Raporlama**
*   **FR-09 (Kullanıcı CRUD):** Yöneticiler yeni kullanıcı oluşturabilmeli, bilgilerini güncelleyebilmelidir.
*   **FR-10 (Dışa Aktarım):** Sistem, işlem geçmişini belirli bir formatta (TXT/CSV) dosya sistemine yazabilmelidir.
*   **FR-11 (Gözlemci):** Yüksek tutarlı işlemler gerçekleştiğinde, "Observer Pattern" devreye girmeli ve yöneticilere otomatik bildirim (simüle e-posta) göndermelidir.

---

# **5. Fonksiyonel Olmayan Gereksinimler (Non-Functional Requirements)**

## **5.1 Güvenilirlik (Reliability)**
*   **NFR-01 (Fallback):** Veritabanı bağlantısı kopsa bile sistem, salt-okunur modda veya önbellekten (In-Memory) çalışmaya devam edebilmelidir.

## **5.2 Taşınabilirlik (Portability)**
*   **NFR-02 (Containerization):** Tüm sistem (Backend, Frontend, DB), tek bir `docker compose up` komutu ile herhangi bir işletim sisteminde ayağa kalkabilmelidir.

## **5.3 Performans (Performance)**
*   **NFR-03 (Response Time):** API yanıt süreleri normal yük altında 500ms'nin altında olmalıdır.

## **5.4 Sürdürülebilirlik (Maintainability)**
*   **NFR-04 (Modularity):** Kod yapısı, katmanlı mimariye (Controller, Service, Repository) uygun olmalı; iş mantığı ile veri erişimi birbirinden ayrılmalıdır.

---

# **6. Sistem Arayüzleri**

## **6.1 Kullanıcı Arayüzleri**
*   Web tabanlı, responsive (duyarlı) tasarım (Flask templates + Bootstrap/CSS).
*   Yönetici paneli ve Kullanıcı dashboard'u ayrılmış görünümler.

## **6.2 Donanım Arayüzleri**
*   Sistem, standart x86-64 mimarili sunucularda veya geliştirici dizüstü bilgisayarlarında çalışacak şekilde tasarlanmıştır.

## **6.3 İletişim Arayüzleri**
*   HTTP/1.1 veya HTTP/2 üzerinden RESTful iletişim.
*   JSON veri formatı.

---
