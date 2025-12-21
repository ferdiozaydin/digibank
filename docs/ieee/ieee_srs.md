# 1. Gereksinim Analizi (IEEE SRS)

Aşağıda DigiBank + Smart City entegrasyon projesi için **IEEE SRS standardına uygun**, akademik formatta hazırlanmış bir bölümü bulacaksın. Bu içerik ödev raporuna **doğrudan eklenebilir niteliktedir**.

---

# **1. Giriş (Introduction)**

Bu dokümanın amacı, Akıllı Şehir Otomasyon Sistemi ile entegre çalışan DigiBank dijital bankacılık platformunun yazılım gereksinimlerini IEEE SRS standardına uygun şekilde tanımlamaktır. Sistem; şehir altyapısı, sensör tabanlı güvenlik mekanizmaları ve akıllı hizmetler ile finansal işlemleri bir arada sunan kapsamlı bir otomasyon çözümüdür. Kullanıcılar, güvenli kimlik doğrulama mekanizmalarıyla sisteme erişebilir, şehir hizmetlerini görüntüleyebilir ve fiat veya kripto para kullanarak ödeme işlemlerini gerçekleştirebilirler.

Bu doküman; sistemin fonksiyonel gereksinimlerini, fonksiyonel olmayan gereksinimlerini, güvenlik kriterlerini, yüksek seviyeli mimarisini ve harici sistemlerle etkileşimlerini tanımlar. Tanımlanan gereksinimler, tasarım, geliştirme, test ve doğrulama süreçlerinin temel referans kaynağı olarak kullanılacaktır.

---

# **2. Sistem Tanımı (System Description)**

DigiBank, Akıllı Şehir çatısı altında çalışan merkezi bir dijital bankacılık bileşenidir. Sistem; şehir sakinlerinin kullanımına sunulan dijital hizmetlerin (otopark, toplu taşıma, enerji tüketimi vb.) ödeme altyapısını sağlar. Aynı zamanda şehir yönetimi tarafından işletilen sensör ağları, güvenlik mekanizmaları ve otomasyon servisleri ile entegre çalışır.

Sistem aşağıdaki ana bileşenlerden oluşur:

* **DigiBank Ödeme Motoru:** Fiat ve kripto para birimleriyle işlem yapılmasını sağlar.
* **Kimlik Doğrulama Servisi:** MFA ve rol tabanlı erişim kontrolü sunar.
* **CityController:** Şehir altyapısını yönetir (trafik, aydınlatma, sensörler).
* **Bildirim Servisi:** Kullanıcılara, kamu güvenliği birimlerine ve diğer paydaşlara gerçek zamanlı uyarılar gönderir.
* **Hibrit Bulut Altyapısı:** Kritik hesap ve işlem verileri yerel sunucularda saklanırken, loglar, analiz çıktıları ve ölçeklenebilir hizmetler bulut ortamında tutulur.

Bu yapı sayesinde hem yüksek güvenlik hem yüksek erişilebilirlik sağlanmaktadır.

---

# **3. Genel Bakış (Overall Description)**

Bu bölüm, sistemin kullanıcı profillerini, genel özelliklerini ve çalışma bağlamını açıklamaktadır.

## **3.1 Kullanıcı Profilleri**

* **Sakin (Resident):** Şehir hizmetlerini görüntüler, ödeme yapar, ev cihazlarını uzaktan yönetir, bildirim alır.
* **CityController Operatörü:** Trafik, sokak aydınlatması, çevresel sensörler ve diğer altyapı birimlerini yönetir.
* **BankingService:** Ödeme işlemlerini doğrular, işlem sonucunu iletir.
* **Kamu Güvenliği Yetkilileri:** Acil durum ve güvenlik ihlali uyarılarını alır.
* **Kamu Hizmetleri Birimleri:** Arıza ve bakım gereksinimlerini gösteren sensör uyarılarına yanıt verir.
* **Sistem Yöneticisi:** Kullanıcı, rol, güvenlik yapılandırması ve sistem performansını yönetir.

## **3.2 Sistem Özellikleri**

* Güvenli kullanıcı kimlik doğrulama
* Şehir hizmetlerinin dijital olarak görüntülenmesi
* Fiat / kripto para ödeme işlemleri
* Gerçek zamanlı bildirim gönderimi
* Şehir altyapısına ilişkin otomatik rutinlerin yürütülmesi
* Sensör tabanlı acil durum yönetimi
* Hibrit bulut üzerine kurulu veri yönetimi
* Tasarım kalıplarıyla modüler, genişletilebilir mimari

## **3.3 Kısıtlar**

* Şifreleme mekanizmaları kuantuma dayanıklı olmalıdır.
* Sistem bulut bağımlılığı nedeniyle internet bağlantısı gerektirir.
* Yüksek kullanıcı sayısı için ölçeklenebilir kaynak yönetimi zorunludur.
* Tüm dış API bağlantılarında güvenli protokol kullanılması gerekmektedir.

## **3.4 Varsayımlar**

* Resmi hizmet sağlayıcı API’leri düzenli olarak erişilebilir durumdadır.
* Kullanıcı cihazları modern tarayıcı veya mobil uygulama desteğine sahiptir.
* Sensör altyapısı şehir genelinde doğru veri iletmektedir.

---

# **4. Sistem Mimarisi (System Architecture)**

DigiBank + Smart City platformu dört temel katman üzerinde çalışır:

---

## **4.1 Uygulama Katmanı (Application Layer)**

Bu katman, kullanıcı etkileşimlerini ve iş mantığının bir kısmını içerir.

* Resident Web/Mobil Uygulaması
* Admin Paneli
* CityController Operasyon Arayüzü

Bu katman, API Katmanı üzerinden çekirdek servislere bağlanır.

---

## **4.2 API ve Servis Katmanı (Service Layer)**

* **Auth Service:** MFA + JWT tabanlı kimlik doğrulama.
* **DigiBank Service:** Fiat ve kripto ödemeler, hesap yönetimi, işlem oluşturma.
* **Notification Service:** Observer tasarım kalıbı ile gerçek zamanlı bildirimler.
* **CityController Service:** Altyapı yönetimi, otomasyon rutinleri, komut işleme.

Bu katman, sistemin iş kurallarını ve iş akışlarını barındırır.

---

## **4.3 Veri ve Entegrasyon Katmanı (Data & Integration Layer)**

* **Central Database:** Hesaplar, kullanıcılar, işlem geçmişi.
* **IoT Data Store:** Sensör verileri.
* **Log Storage (Cloud):** Uygulama logları, dışa aktarılmış veri dosyaları.
* **Payment Integrations:** Banka API'leri, blockchain sağlayıcıları.

Bu katman hibrit bulut mimarisinin merkezini oluşturur.

---

## **4.4 Hibrit Bulut Altyapısı (Hybrid-Cloud Infrastructure)**

* Yerel Sunucu Katmanı (on-prem) → kritik veriler ve güvenlik modülleri
* Bulut Servisleri (public cloud) → log yönetimi, analitik, ölçekleme
* API Gateway ve yük dengeleme alt yapısı
* Güvenlik duvarı, IAM rolleri, ağ güvenliği politikaları

Bu yaklaşım hem yüksek güvenlik hem de esneklik sağlar.

---
