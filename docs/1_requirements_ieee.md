# 1. Gereksinim Analizi (IEEE Formatı)

## **Fonksiyonel Gereksinimler (Functional Requirements)**

### **FR-01 Dijital Ödeme İşlemleri**

Sistem, kullanıcıların hem fiat (geleneksel para birimi) hem de kripto para ile ödeme yapabilmesini sağlamalıdır. Ödeme işlemleri güvenli bir altyapı üzerinden gerçekleştirilmelidir.

### **FR-02 Güvenli Kullanıcı Girişi**

Sistem, çok faktörlü kimlik doğrulama (MFA) ve rol tabanlı erişim kontrolü ile kullanıcıların güvenli giriş yapmasını desteklemelidir.

### **FR-03 Şehir Hizmetlerinin Görüntülenmesi ve Ödenmesi**

Kullanıcı, park ücreti, toplu taşıma bileti, kamu hizmetleri gibi şehir servislerini görüntüleyebilmeli ve seçilen hizmet için ödeme yapabilmelidir.

### **FR-04 İşlem Kaydı Yönetimi**

Sistem, gerçekleştirilen tüm ödeme ve işlem aktiviteleri için ayrıntılı işlem kayıtları (transaction logs) tutmalıdır. Loglar gerektiğinde incelenebilir olmalıdır.

### **FR-05 Şehir Otomasyon Rutinleri**

CityController bileşeni; sokak aydınlatması, trafik ışıkları ve diğer şehir altyapısı üzerinde otomatik rutinleri çalıştırabilmelidir.

### **FR-06 Sensör Olay Yönetimi**

Sistem, güvenlik veya çevresel sensörlerden gelen olayları gerçek zamanlı işleyebilmeli ve ilgili kamu birimlerine bildirim göndermelidir.

---

## **Fonksiyonel Olmayan Gereksinimler (Non-Functional Requirements)**

### **NFR-01 Güvenli İletişim**

Sistem içindeki tüm veri iletimi uçtan uca şifreleme (end-to-end encryption) ile korunmalıdır.

### **NFR-02 Hibrit Bulut Mimarisi**

Sistem, hem yerel (on-prem) bileşenleri hem de bulut tabanlı servisleri kullanarak hibrit bulut altyapısı üzerinde çalışmalıdır. Bulut depolama (loglar, analiz verileri vb.) entegre olmalıdır.

### **NFR-03 Performans ve Ölçeklenebilirlik**

Sistem, en az 10.000 eşzamanlı kullanıcıyı destekleyebilecek performans kapasitesine sahip olmalıdır.

### **NFR-04 Modüler Mimari**

Sistem, nesne yönelimli prensipleri ve tasarım kalıplarını (Singleton, Observer, Command, Adapter, Template Method vb.) kullanacak şekilde modüler ve genişletilebilir olarak tasarlanmalıdır.

---

## **4.3 Güvenlik Gereksinimleri (Security Requirements)**

### **SEC-01 Kuantuma Dayanıklı Şifreleme**

Sistem, kritik veri ve işlem kanallarında kuantuma dayanıklı kriptografik yöntemleri desteklemelidir.

### **SEC-02 Şüpheli İşlem Tespiti**

Sistem, şüpheli işlem tespitinde dinamik karşı önlem ve anlık uyarı mekanizmaları uygulamalıdır. Bu durumlarda sistem ilgili birimleri bilgilendirmelidir.

---

## **4.4 Harici Entegrasyon Gereksinimleri (External Interface Requirements)**

### **EXT-01 Finansal Servis Entegrasyonları**

Sistem, üçüncü taraf fiat bankacılık API'leri ve kripto para sağlayıcıları ile güvenli API tabanlı entegrasyon gerçekleştirmelidir.

### **EXT-02 Kamu Güvenliği ve Kamu Hizmetleri Entegrasyonu**

Sistem, acil durum, güvenlik ihlali veya arıza gibi olaylarda kamu güvenliği birimleri ve kamu hizmetleri kuruluşlarına gerçek zamanlı bildirim gönderebilmelidir.

---

## **4.5 Özet Gereksinim Tablosu**

| Kategori            | Kod    | Gereksinim                                     |
| ------------------- | ------ | ---------------------------------------------- |
| Fonksiyonel         | FR-01  | Fiat ve kripto ödeme işlemleri                 |
| Fonksiyonel         | FR-02  | Güvenli MFA giriş                              |
| Fonksiyonel         | FR-03  | Şehir hizmetlerinin görüntülenmesi ve ödenmesi |
| Fonksiyonel         | FR-04  | İşlem loglarının tutulması                     |
| Fonksiyonel         | FR-05  | Şehir otomasyon rutinlerinin yönetimi          |
| Fonksiyonel         | FR-06  | Sensör olaylarının işlenmesi                   |
| Fonksiyonel Olmayan | NFR-01 | Uçtan uca şifreleme                            |
| Fonksiyonel Olmayan | NFR-02 | Hibrit bulut entegrasyonu                      |
| Fonksiyonel Olmayan | NFR-03 | 10.000 eşzamanlı kullanıcı desteği             |
| Fonksiyonel Olmayan | NFR-04 | Modüler mimari & tasarım kalıpları             |
| Güvenlik            | SEC-01 | Kuantuma dayanıklı şifreleme                   |
| Güvenlik            | SEC-02 | Şüpheli işlem karşı önlemleri                  |
| Harici Entegrasyon  | EXT-01 | Banka/kripto API entegrasyonu                  |
| Harici Entegrasyon  | EXT-02 | Kamu birimleri bildirim entegrasyonu           |
