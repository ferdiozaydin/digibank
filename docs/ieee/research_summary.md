# Araştırma Özeti: Klasik Bankacılıktan Akıllı Şehir Finansına Dönüşüm

Bu çalışma, **DigiBank** projesi üzerinden, geleneksel bir masaüstü uygulamasının (`digibank.exe`) modern, servis odaklı ve akıllı şehir entegrasyonuna sahip bir web platformuna evrilmesini incelemektedir. Araştırma, yazılım mimarisi modernizasyonu, OOP tasarım kalıplarının pratik uygulamaları ve dağıtık sistemlerin yönetimi konularına odaklanmaktadır.

Projenin temel bulguları ve teknik kazanımları şunlardır:

### 1. Mimari Dönüşüm (Legacy to Modern)
Geleneksel "stand-alone" (tek başına çalışan) masaüstü uygulamalarının, ölçeklenebilirlik ve erişilebilirlik sorunları nedeniyle modern ihtiyaçlara cevap veremediği gözlemlenmiştir.
*   **Ayrıklaşma (Decoupling):** Tek bir exe dosyası yerine, **Backend (Java)** ve **Frontend (Flask)** olarak ayrılan yapı, geliştirme süreçlerini hızlandırmış ve bakım maliyetlerini düşürmüştür.
*   **Konteynerizasyon:** Docker kullanımı, "benim makinemde çalışıyor" sorununu ortadan kaldırarak tutarlı bir dağıtım (deployment) ortamı sağlamıştır.

### 2. Akıllı Şehir Entegrasyonu
Finansal sistemlerin sadece para transferi aracı olmaktan çıkıp, yaşam alanlarıyla etkileşime giren platformlara dönüşümü modellenmiştir.
*   **Entegre Ödemeler:** Su, elektrik gibi şehir faturalarının bankacılık arayüzünden, farklı para birimleriyle (Fiat/Kripto) ödenebilmesi sağlanmıştır.
*   **IoT Yönetimi:** Finansal arayüz üzerinden ev otomasyon cihazlarının (termostat, kilit) kontrol edilmesi, "Smart Resident" (Akıllı Sakin) kavramını somutlaştırmıştır.

### 3. Tasarım Kalıplarının Etkisi
Standart GoF tasarım kalıplarının, karmaşık iş problemlerini çözmede kritik rol oynadığı görülmüştür:
*   **Adapter Pattern:** Farklı ödeme altyapılarının (Blockchain vs. Banka) tek bir arayüzden yönetilmesini sağlayarak kod tekrarını önlemiştir.
*   **Observer Pattern:** Sistem olaylarının (yüksek tutarlı transferler gibi) gevşek bağlı (loosely coupled) modüller tarafından dinlenmesine ve otomatik aksiyon alınmasına olanak tanımıştır.
*   **Command Pattern:** İşlemlerin (Transaction) birer nesne olarak kapsüllenmesi, işlem geçmişinin izlenmesini ve hata durumunda geri alınabilirlik (rollback) potansiyelini artırmıştır.

### 4. Güvenlik ve Uyumluluk
Prototip aşamasında dahi olsa, finansal yazılımların güvenlikten ödün veremeyeceği prensibi benimsenmiştir.
*   **MFA (Çok Faktörlü Kimlik Doğrulama):** Parola güvenliğinin ötesine geçilerek TOTP ile ikinci bir doğrulama katmanı eklenmiştir.
*   **Veri Bütünlüğü:** Hassas verilerin (parola) kriptografik özetleme (hashing) ile saklanması sağlanmıştır.

### Sonuç
Bu araştırma, eski nesil bankacılık yazılımlarının modern web teknolojileri ve doğru mimari desenlerle dönüştürülebileceğini kanıtlamaktadır. Ortaya çıkan hibrit yapı, hem geleneksel bankacılık güvenliğini korumakta hem de akıllı şehirlerin getirdiği yenilikçi ihtiyaçlara esnek bir şekilde uyum sağlayabilmektedir. Gelecek çalışmalarda, simülasyon ortamlarının (Blockchain, Email) gerçek dünya entegrasyonlarına dönüştürülmesi planlanmaktadır.
