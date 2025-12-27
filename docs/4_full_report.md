# DigiBank & Smart City – Proje Raporu

- **Öğrenci:** Ferdi Özaydın / 2420003042
- **Ders:** Nesne Tabanlı Programlama
- **Tarih:** 27.12.2025
- **Proje Kaynak Kodları:** [https://github.com/ferdiozaydin/digibank](https://github.com/ferdiozaydin/digibank)

---

## 1. Özet

Bu proje, geleneksel bankacılık işlemlerini modern "Akıllı Şehir" (Smart City) konseptleriyle birleştiren, güvenli, modüler ve ölçeklenebilir bir yazılım sistemi prototipidir. Çalışma iki ana evreden oluşmaktadır:

1.  **Analiz Evresi:** Referans alınan "digibank.exe" isimli konsol tabanlı C++ uygulaması analiz edilmiştir. Bu uygulama, projenin temel gereksinimlerini belirlemek için bir kıyaslama noktası olarak kullanılmış, ancak tarafımızca geliştirilmemiştir.
2.  **Geliştirme Evresi:** Referans sistemin sunduğu temel fonksiyonlar baz alınarak, tamamen modern teknolojilerle (Java 17, Python Flask, PostgreSQL, Docker) donatılmış, "DigiCityBank" adı verilen özgün bir sistem geliştirilmiştir.

Proje, Nesne Tabanlı Programlama (OOP) prensiplerini, yaygın Tasarım Kalıplarını (Design Patterns) ve modern güvenlik uygulamalarını (MFA, kriptografik hashing) pratik bir senaryoda göstermeyi amaçlar. Ayrıca Docker konteynerizasyonu, veritabanı entegrasyonu (PostgreSQL) ve e-posta simülasyonu (Mailpit) gibi endüstri standartları da projenin altyapısına dahil edilmiştir.

## 2. Giriş

### 2.1. Problem Tanımı

Günümüzde bankacılık sistemleri sadece para transferi yapılan finansal aracılar olmaktan çıkmıştır. Dijitalleşen dünyada bankalar, kullanıcıların fatura ödemelerini, aboneliklerini, akıllı ev sistemlerini ve hatta kripto varlıklarını tek bir merkezden yönettiği "Süper Uygulamalara" dönüşmektedir. Ancak bu entegrasyon süreci; veri güvenliği, sistemlerin birbiriyle konuşması (interoperability) ve ölçeklenebilirlik gibi karmaşık mühendislik problemlerini beraberinde getirmektedir. Geleneksel monolitik yapılar bu ihtiyaçlara cevap vermekte zorlanmaktadır.

### 2.2. Projenin Amacı

Bu projenin temel amacı, modern bir finansal teknolojinin (FinTech) ihtiyaç duyduğu altyapıyı akademik bir perspektifle simüle etmektir.
Hedefler şunlardır:

1.  **OOP Prensiplerinin Uygulanması:** Soyutlama, kalıtım, çok biçimlilik ve kapsülleme gibi prensiplerin gerçek hayat problemlerinde nasıl kullanıldığını göstermek.
2.  **Mimari Dönüşüm:** Basit bir konsol uygulamasından (digibank.exe), çok katmanlı ve servis odaklı modern bir web mimarisine (DigiCityBank) geçiş sürecini yönetmek.
3.  **Tasarım Kalıpları:** Singleton, Strategy, Observer, Adapter ve Command gibi kalıpları kullanarak kodun esnekliğini ve bakım kolaylığını artırmak.
4.  **Güvenlik:** Kullanıcı verilerini korumak için endüstri standardı güvenlik önlemlerini (Salted Hashing, TOTP MFA) entegre etmek.

## 3. Sistem Tasarımı ve Yöntem

### 3.1. Yazılım Mimarisi

Sistem, modern mikroservis yaklaşımlarına benzer şekilde, sorumlulukların ayrıldığı (Separation of Concerns) dağıtık bileşenlerden oluşur:

- **Sunucu Katmanı (Backend - Java 17):** Sistemin beyni olarak görev yapar. Harici bir web sunucusu (Tomcat vb.) yerine, Java'nın yerleşik `HttpServer` sınıfı kullanılarak native HTTP işleme yeteneği sergilenmiştir. Tüm iş kuralları, veri doğrulama, veritabanı iletişimi ve güvenlik kontrolleri bu katmanda yönetilir.
- **İstemci Katmanı (Frontend - Python Flask):** Kullanıcı etkileşiminin gerçekleştiği web arayüzüdür. Modern HTML5/CSS3 ve JavaScript kullanılarak tasarlanmış, Python Flask framework'ü üzerinde çalışmaktadır. Backend ile RESTful API üzerinden JSON formatında haberleşir.
- **Veritabanı Katmanı (PostgreSQL):** Veri kalıcılığını sağlamak için ilişkisel veritabanı yönetim sistemi olan PostgreSQL kullanılmıştır. Kullanıcı bilgileri, işlemler ve sistem logları burada tutulur.
- **Yan Servisler (Mailpit):** Sistemin e-posta gönderme yeteneklerini test etmek için kullanılan, gerçek dünya senaryolarını simüle eden bir SMTP sunucusudur.

### 3.2. Kullanılan Tasarım Kalıpları (Design Patterns)

Projede karşılaşılan yapısal sorunlar, kanıtlanmış tasarım kalıpları ile çözülmüştür:

- **Singleton:** `AuditLogger` sınıfı ile tüm sistemde tek bir loglama mekanizması garanti edilmiş, böylece eşzamanlı erişim sorunları önlenmiştir.
- **Strategy:** Ödeme sisteminde esneklik sağlamak için `PaymentStrategy` arayüzü kurgulanmıştır. Bu sayede FIAT para veya Kripto para ile ödeme algoritmaları çalışma zamanında (runtime) seçilebilir hale gelmiştir (Open/Closed Principle).
- **Adapter:** Farklı arayüzlere sahip dış kripto servislerini sisteme entegre etmek için `CryptoAdapter` kullanılmıştır.
- **Observer:** Akıllı ev (Smart Home) modülünde, sensör verilerindeki değişimlerin ilgili cihazlara (klima, ışıklandırma) otomatik bildirilmesi için bu desen kullanılmıştır.
- **Command:** Yönetici komutları ve ev otomasyon işlemleri birer nesne (Command Object) olarak modellenmiş, bu sayede işlemlerin sıraya alınması, geri alınması (undo) ve loglanması kolaylaşmıştır.

### 3.3. Güvenlik Altyapısı

Güvenlik, projenin en kritik bileşenlerinden biridir:

- **Parola Güvenliği:** Kullanıcı parolaları asla düz metin (plaintext) olarak saklanmaz. SHA3-512 algoritması ve rastgele üretilen "Salt" değerleri ile hashlenerek veritabanında tutulur.
- **Çok Faktörlü Kimlik Doğrulama (MFA):** Giriş güvenliğini artırmak için Zaman Tabanlı Tek Kullanımlık Şifre (TOTP) algoritması uygulanmıştır.
- **Yetkilendirme:** Rol tabanlı erişim kontrolü (RBAC) ile "Admin" ve "Standart Kullanıcı" yetkileri ayrıştırılmış, kritiik operasyonlar sadece yetkili kullanıcılara açılmıştır.

---

## 4. Kullanıcı Arayüzleri ve Uygulama Görüntüleri

Projenin evrimini ve geldiği noktayı belgelemek adına, hem referans alınan eski sistem hem de geliştirilen yeni sistemin ekran görüntüleri aşağıda sunulmuştur.

### 4.1. Bölüm 1: DigiBank Konsol Uygulaması (Referans Proje)

Bu projenin geliştirilmesinde analiz ve kıyaslama amacıyla kullanılan, C++ tabanlı `digibank.exe` referans uygulamasının arayüzleri aşağıdadır. Bu uygulama tarafımızca geliştirilmemiş, sadece gereksinim analizinde kullanılmıştır.

**1. Ana Menü ve Giriş**
Kullanıcıyı karşılayan ana menü, tüm operasyonlara (Ekleme, Silme, Listeleme vb.) erişim sağlayan basit bir metin arayüzüdür.
![DigiBank Ana Menü](../screenshots/digibank/digibank_main.png)

**2. Veri Ekleme**
Yeni bir banka veya müşteri kaydı oluşturmak için kullanılan veri giriş ekranı.
![DigiBank Ekle](../screenshots/digibank/digibank_ekle.png)

**3. Veri Listeleme**
Sistemde kayıtlı olan tüm verilerin listelendiği ekran.
![DigiBank Liste](../screenshots/digibank/digibank_liste.png)

**4. Arama İşlemi**
Belirli kriterlere göre kayıtlar arasında arama yapılmasını sağlayan fonksiyon.
![DigiBank Ara](../screenshots/digibank/digibank_ara.png)

**5. Kayıt Düzenleme**
Mevcut bir kaydın bilgilerini güncellemek için kullanılan ekran.
![DigiBank Düzelt](../screenshots/digibank/digibank_duzelt.png)

**6. Kayıt Silme**
Veritabanından bir kaydın silinmesi işlemi.
![DigiBank Sil](../screenshots/digibank/digibank_sil.png)

---

### 4.2. Bölüm 2: DigiCityBank Web Platformu (Geliştirilen Modern Sistem)

Referans projenin kısıtları aşılarak geliştirilen, modern web teknolojileri ile donatılmış DigiCityBank platformunun arayüzleri aşağıdadır.

**1. Güvenli Giriş Ekranı**
Kullanıcı adı ve parola doğrulamasının yapıldığı, arka planda MFA ve güvenli oturum yönetiminin çalıştığı giriş kapısı.
![Giriş Ekranı](../screenshots/digicitybank-login.png)

**2. Yönetim Paneli (Dashboard)**
Kullanıcının finansal durumunu, varlıklarını ve son hareketlerini bir bakışta görebildiği, kullanıcı deneyimi odaklı ana ekran.
![Dashboard](../screenshots/digicitybank-dashboard.png)

**3. Performans ve Analitik**
Yöneticiler için hazırlanan, sistemin işlem hacmini, finansal tahminleri (Forecast) ve sunucu sağlık durumunu gösteren grafiksel analiz ekranı.
![Performans Metrikleri](../screenshots/digicitybank-performance.png)

**4. Akıllı Şehir (Smart Government) Ödemeleri**
Elektrik, su, doğalgaz gibi temel faturaların ödendiği, şehir altyapısı ile entegre modül.
![Smart City Ödemeleri](../screenshots/digicitybank-smart-city.png)

**5. Para Transferi**
Kullanıcılar arası para transferini (EFT/Havale) simüle eden, bakiye kontrolü yapan arayüz.
![Para Transferi](../screenshots/digicitybank-bank-transfer.png)

**6. Akıllı Ev (Smart Home) Kontrolü**
Bankacılık uygulaması üzerinden evdeki IoT cihazlarının (Termostat, Işıklar) yönetildiği, Observer deseninin uygulandığı modül.
![Akıllı Ev Kontrolü](../screenshots/digicitybank-smart-home.png)

**7. Yönetici: Kullanıcı Listesi**
Sistem yöneticisinin kayıtlı kullanıcıları yönettiği, yetki ataması yapabildiği panel.
![Kullanıcı Yönetimi](../screenshots/digicitybank-admin-user-list.png)

**8. Yönetici: Finansal İşlemler**
Gerçekleşen tüm para transferlerinin ve ödemelerin dökümünün alındığı denetim (audit) ekranı.
![İşlem Listesi](../screenshots/digicitybank-bank-transactions-list.png)

**9. Excel Raporlama**
Finansal verilerin muhasebe sistemlerine aktarımı için Excel formatında dışa aktarılmasını sağlayan özellik.
![Excel Export](../screenshots/digicitybank-bank-transactions-excel.png)

**10. E-Posta Servisi Entegrasyonu**
Sistemden gönderilen bildirimlerin yakalandığı sanal SMTP sunucusu (Mailpit) arayüzü.
![E-Posta Testi](../screenshots/digicitybank-bank-transactions-mailpit.png)

**11. İşlem Bildirimi**
Kullanıcıya işlemin başarılı olduğuna dair gönderilen e-posta ve web bildirimi.
![E-Posta Başarılı](../screenshots/digicitybank-bank-transactions-mail-success.png)

---

## 5. Sonuç ve Değerlendirme

Bu proje çalışması ile "DigiBank" referans fikrinden yola çıkılarak, modern yazılım mühendisliği prensiplerine tam uyumlu "DigiCityBank" ekosistemi başarıyla hayata geçirilmiştir. Süreç boyunca elde edilen teknik ve akademik kazanımlar aşağıda detaylandırılmıştır:

1.  **Tam Yığın (Full-Stack) Mimari Yetkinliği:**

    - Proje, sadece tek bir dilde kod yazmanın ötesine geçerek, **Java** (Backend) ve **Python** (Frontend) gibi farklı teknolojilerin bir arada nasıl uyum içinde çalışabileceğini (Interoperability) kanıtlamıştır.
    - REST API standartlarına uygun servisler kurgulanarak, istemci ve sunucu arasındaki bağımlılık azaltılmış, sistem modüler hale getirilmiştir.

2.  **Nesne Tabanlı Tasarımın Gücü:**

    - Teorik olarak öğrenilen Tasarım Kalıplarının (Design Patterns), kodun kalitesini, okunabilirliğini ve değiştirilebilirliğini ne denli artırdığı pratikte gözlemlenmiştir. Özellikle **Observer** deseni ile sistemin reaktif (tepkisel) hale gelmesi ve **Strategy** deseni ile yeni ödeme yöntemlerinin sisteme kolayca eklenebilmesi, doğru tasarımın önemini ortaya koymuştur.

3.  **Güvenlik Farkındalığı:**

    - Bir fintech uygulamasında "güvenlik sonradan eklenemez, tasarımla gelir" (Security by Design) ilkesi benimsenmiştir. Parolaların hashlenmesi, MFA kullanımı ve yetki kontrolleri gibi önlemler, güvenli yazılım geliştirme süreçlerinin bir parçası olmuştur.

4.  **Operasyonel Mükemmellik (DevOps):**
    - Uygulamanın **Docker** konteynerleri üzerinde çalıştırılması, "benim makinemde çalışıyordu" sorununu ortadan kaldırmış ve dağıtım süreçlerini standartlaştırmıştır. Bu sayede proje, herhangi bir ortamda (Local, Cloud) hızlıca ayağa kaldırılabilir hale gelmiştir.

**Gelecek Çalışmalar ve Öneriler:**
Mevcut prototip başarıyla çalışmakla birlikte, ticari bir ürüne dönüşmesi için şu geliştirmeler planlanmaktadır:

- Gerçek blockchain ağları (Ethereum vb.) ile entegrasyon sağlanarak kripto ödemelerinin simülasyondan çıkarılması.
- Mikroservis mimarisinin daha ileri seviyeye taşınarak her modülün (User Service, Payment Service, Notification Service) ayrı konteynerlerde çalıştırılması.
- Yük testleri yapılarak sistemin yüksek trafik altındaki davranışının analiz edilmesi.

Özetle; DigiCityBank projesi, modern yazılım geliştirme araçlarını ve yöntemlerini bir araya getiren, akademik derinliği olan ve endüstriyel standartları yakalayan kapsamlı bir mühendislik çalışmasıdır.

## 6. Ekler

- **Kaynak Kodlar:** GitHub Reposu / Proje Klasörü
