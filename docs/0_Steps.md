Projede yapman gerekenleri özet adım listesi şöyle:

1. **Gereksinim Analizi**
   * DigiBank dijital bankacılık sistemi için 1–2 paragraf sistem gereksinimi yaz.

2. **UML Diyagramları**

   * Smart city + DigiBank için UML Sınıf/Sistem diyagramı çiz.
   * Ayrı olarak detaylı **Use Case** diyagramı çiz (Resident, CityController, BankingService vb. aktörler).

3. **Sözde Kod (Pseudocode)**

   * Temel akışlar için sözde kod yaz:

     * Örnek: “kullanıcı giriş yapar → bakiye görüntüle → ödeme yap → sonuç bildirimi gönder”

4. **GUI Tasarımı (Mock-up)**

   * Kullanıcıların giriş yapacağı, bakiye göreceği, ödeme yapacağı ekranları çiz (Figma, Draw.io vb.).
   * Yöneticiler için basit bir dashboard taslağı ekle (ışıklar, trafik, alarmlar).

5. **Hibrit Bulut Dağıtımı**

   * DigiBank Alpha hibrit-cloud paketini bir public cloud ortamına kur.
   * Karşılaştığın risk ve gereksinimleri not al (raporda kullanacaksın).

6. **JAVA ile Temel Uygulama**

   * Eclipse kur.
   * Aşağıdakileri içeren basit bir prototip yaz (tercihen JAVA):

     * Kullanıcı sınıfları, hesap/işlem sınıfları
     * BankingService, CityController vb.
     * Önerilen pattern’lerden en az birkaçını kullan (Singleton, Command, Observer, Adapter, Template Method).

7. **Smart-Government Entegrasyonu**

   * Smart-government tarafını (ör: vergi ödeme, e-devlet işlemi, fatura ödeme) DigiBank ile etkileşime sokan sınıflar/ekranlar ekle.
   * Bunu kodda ve GUI’de göster.

8. **Araştırma Özeti (Task 2.A)**

   * Nesne yönelimli tasarım özellikleri ve olası araştırma konuları için **1 paragraf** kısa özet yaz.

9. **Konferans/Jurnal Raporu (Task 2.B)**

   * IEEE konferans şablonunda birkaç sayfalık teknik makale hazırla:

     * Giriş, Sistem Tasarımı, Tasarım Kalıpları, Hibrit Bulut, Güvenlik, Sonuçlar, Gelecek Çalışmalar.

10. **Ekran Görüntüleri**

    * UML, GUI mock-up, çalışan uygulama ve çıktı ekranlarının hepsinin screenshot’larını al.

11. **Tam Rapor**

    * Tüm kısımları (analiz, diyagramlar, kod açıklaması, test sonuçları, ekran görüntüleri, makale) tek bir raporda topla (PDF).

12. **Zip Dosyası**

    * Tek bir zip hazırla:

      * Kaynak kod
      * UML diyagramları
      * Pseudocode
      * Ekran görüntüleri
      * “Karmaşıklık kaynakları ve modüler çözüm” notların
    * İsim: `studentID_fullName.zip`

13. **Bonus için (İsteğe bağlı)**

    * DigiBank uygulamasını çalıştır.
    * Bugünün timestamp’li bir txt export et.
    * Birkaç kullanıcı kaydet.
    * Observer pattern ile `./txt` klasörünü izleyip, yeni txt oluşunca kendine mail atılmasını sağla (veya simüle et).
    * Uygulama, txt ve e-posta zaman damgalı ekran görüntülerini al.

