# Sensör Platformu

Spring Boot ve React öğrenmek amacıyla sıfırdan geliştirdiğim bir sensör yönetim uygulaması.
Sensörler kaydedilir, listelenir ve pasifleştirilir. Proje adım adım genişlemeye devam ediyor (bkz. [Yol haritası](#yol-haritası)).

## Teknolojiler

| Katman | Kullanılanlar |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Data JPA (Hibernate), Bean Validation, Spring Boot Actuator |
| Veritabanı | PostgreSQL 16, Flyway |
| Test | JUnit 5, Mockito, Testcontainers, MockMvc |
| Frontend | React 19, TypeScript, Vite, MUI |
| Altyapı | Docker Compose |

## Proje yapısı

```
├─ docker-compose.yml   PostgreSQL
├─ sensor.http          Elle API testleri (VS Code REST Client)
├─ sensor-service/      Spring Boot REST API
└─ frontend/            React + TypeScript arayüz
```

## Çalıştırma

Gereksinimler: JDK 21, Maven, Node.js, Docker.

```bash
# 1) Veritabanı
docker compose up -d

# 2) Backend  →  http://localhost:8080
cd sensor-service
mvn spring-boot:run

# 3) Frontend →  http://localhost:5173
cd frontend
npm install
npm run dev
```

Testler (Docker açık olmalı; testler kendi PostgreSQL konteynerini başlatır):

```bash
cd sensor-service
mvn test
```

## API

| Metot | Adres | Açıklama | Hata durumları |
|---|---|---|---|
| `POST` | `/api/sensors` | Sensör oluşturur, `201` ve `Location` başlığı döner | `400` geçersiz girdi, `409` mükerrer isim |
| `GET` | `/api/sensors` | Tüm sensörleri listeler | |
| `GET` | `/api/sensors/{id}` | Tek sensörü getirir | `404` |
| `POST` | `/api/sensors/{id}/deactivate` | Sensörü pasifleştirir | `404`, `409` zaten pasif veya eşzamanlı değişiklik |

Hata cevapları RFC 9457 `ProblemDetail` formatındadır. Doğrulama hatalarında `errors` alanı, hangi alanın neden reddedildiğini listeler.

## Öne çıkan tasarım kararları

- **Mükerrer isim iki katmanda kontrol ediliyor.** Servisteki kontrol kullanıcıya anlaşılır bir hata verir; asıl garanti veritabanındaki `UNIQUE` kısıtıdır. Eşzamanlı iki istek servis kontrolünü birlikte geçerse, veritabanı hatası da aynı `409` cevabına çevrilir.
- **İyimser kilitleme (`@Version`).** Aynı kaydın eşzamanlı güncellenmesinde son yazanın öncekinin üzerine sessizce yazması (lost update) engellenir. Bu davranış ayrı bir testle doğrulanır.
- **Value object.** Konum, kendi geçerliliğini garanti eden değişmez bir `GeoLocation` record'u olarak modellenir. Aralık kontrolü hem API girişinde (kullanıcıya alan bazında hata) hem de modelde (her yoldan gelen veri için garanti) yapılır.
- **Davranışı olan entity.** Durum değişiklikleri setter yerine `deactivate()` gibi kuralı kendi içinde taşıyan metotlarla yapılır.
- **Entity yerine DTO.** API sözleşmesi veritabanı yapısından ayrıdır; istemci sadece izin verilen alanları gönderebilir.
- **İç hata ayrıntıları sızdırılmaz.** Veritabanı ve Hibernate istisnalarının mesajları istemciye gönderilmez; ayrıntı log'a yazılır.
- **Gerçek veritabanıyla test.** Entegrasyon testleri Testcontainers ile her çalıştırmada boş bir PostgreSQL üzerinde çalışır; Flyway migration'ları da bu sırada doğrulanmış olur.
- **CORS yerine proxy.** Geliştirmede arayüz, istekleri Vite proxy üzerinden backend'e iletir; arayüz kodunda backend adresi yer almaz.

## Yol haritası

- [x] Sensör CRUD, doğrulama, hata yönetimi
- [x] Birim ve entegrasyon testleri
- [x] React arayüzü: listeleme ve pasifleştirme
- [ ] Sensör ekleme formu
- [ ] Servisin Docker imajı
- [ ] Sensörlerden UDP ile veri alımı (Netty), son durum için Redis
- [ ] Kafka ile olay yayını ve ayrı bir alarm servisi
- [ ] WebSocket ile canlı izleme ekranı
- [ ] CI (GitHub Actions)
