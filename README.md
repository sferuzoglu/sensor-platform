# Sensör Platformu

Spring Boot ve React öğrenmek amacıyla sıfırdan geliştirdiğim bir sensör yönetim uygulaması.
Sensörler arayüzden eklenir, listelenir, pasifleştirilir ve silinir. Sensörlerin UDP ile gönderdiği ölçümler alınır; her sensörün son değeri ve çevrimiçi durumu arayüzde gösterilir. Proje adım adım genişlemeye devam ediyor (bkz. [Yol haritası](#yol-haritası)).

## Teknolojiler

| Katman | Kullanılanlar |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Data JPA (Hibernate), Bean Validation, Spring Boot Actuator, Netty (UDP) |
| Veritabanı | PostgreSQL 16, Flyway, Redis 7 (son durum) |
| Test | JUnit 5, Mockito, Testcontainers, MockMvc |
| Frontend | React 19, TypeScript, Vite, MUI |
| Altyapı | Docker (multi-stage build), Docker Compose |

## Proje yapısı

```
├─ docker-compose.yml   PostgreSQL + Redis + sensor-service
├─ simulator/           UDP sensör simülatörü (tek dosya, bağımlılıksız)
├─ docs/protocol.md     Sensör UDP protokolü (ICD)
├─ sensor.http          Elle API testleri (VS Code REST Client)
├─ sensor-service/      Spring Boot REST API + UDP veri alımı
└─ frontend/            React + TypeScript arayüz
```

## Çalıştırma

**Seçenek A: Backend Docker'da** (gereksinimler: Docker, Node.js)

```bash
# 1) Veritabanı + backend  →  http://localhost:8080
docker compose up -d --build

# 2) Frontend →  http://localhost:5173
cd frontend
npm install
npm run dev
```

**Seçenek B: Backend yerelde** (geliştirme için; gereksinimler: JDK 21, Maven, Node.js, Docker)

```bash
# 1) Sadece altyapı (Docker'daki backend çalışıyorsa önce: docker compose stop sensor-service)
docker compose up -d postgres redis

# 2) Backend  →  http://localhost:8080
cd sensor-service
mvn spring-boot:run

# 3) Frontend: Seçenek A'daki gibi
```

Sensör verisi göndermek için (repo kökünden, PowerShell):

```powershell
java simulator/SensorSimulator.java --sensors ((Invoke-RestMethod http://localhost:8080/api/sensors).id -join ",")
```

Simülatör kayıp, tekrar, sıra karışması, bozuk paket ve yeniden başlama senaryolarını da üretebilir; seçenekler dosyanın başında açıklanmıştır.

Testler (Docker açık olmalı; testler kendi PostgreSQL ve Redis konteynerlerini başlatır):

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
| `DELETE` | `/api/sensors/{id}` | Sensörü siler, `204` döner (gövdesiz) | `404` |
| `GET` | `/api/sensors/{id}/latest` | Son ölçüm (`sequence`, `timestamp`, `value`). Sensör 10 saniyedir veri göndermiyorsa `204` | `404` |

Hata cevapları RFC 9457 `ProblemDetail` formatındadır. Doğrulama hatalarında `errors` alanı, hangi alanın neden reddedildiğini listeler.

## Sensör verisi (UDP)

Sensörler ölçümlerini **UDP 9876** portuna 31 baytlık ikili paketler olarak gönderir. Paket formatı, doğrulama kuralları ve örnek paket [docs/protocol.md](docs/protocol.md) dosyasındadır.

Elle bir paket göndermek için (PowerShell):

```powershell
$udp = New-Object System.Net.Sockets.UdpClient
$p = [byte[]](0x53,0x46,0x01,0x00,0x01,0x00,0x10,0x00,0x00,0x00,0x2A,0x00,0x00,0x00,0x07,0x00,0x00,0x01,0x9B,0x76,0xDA,0xA8,0x00,0x40,0x35,0x80,0x00,0x00,0x00,0x00,0x00)
$udp.Send($p, $p.Length, "localhost", 9876)
```

## Öne çıkan tasarım kararları

- **Mükerrer isim iki katmanda kontrol ediliyor.** Servisteki kontrol kullanıcıya anlaşılır bir hata verir; asıl garanti veritabanındaki `UNIQUE` kısıtıdır. Eşzamanlı iki istek servis kontrolünü birlikte geçerse, veritabanı hatası da aynı `409` cevabına çevrilir.
- **İyimser kilitleme (`@Version`).** Aynı kaydın eşzamanlı güncellenmesinde son yazanın öncekinin üzerine sessizce yazması (lost update) engellenir. Bu davranış ayrı bir testle doğrulanır.
- **Value object.** Konum, kendi geçerliliğini garanti eden değişmez bir `GeoLocation` record'u olarak modellenir. Aralık kontrolü hem API girişinde (kullanıcıya alan bazında hata) hem de modelde (her yoldan gelen veri için garanti) yapılır.
- **Davranışı olan entity.** Durum değişiklikleri setter yerine `deactivate()` gibi kuralı kendi içinde taşıyan metotlarla yapılır.
- **Entity yerine DTO.** API sözleşmesi veritabanı yapısından ayrıdır; istemci sadece izin verilen alanları gönderebilir.
- **İç hata ayrıntıları sızdırılmaz.** Veritabanı ve Hibernate istisnalarının mesajları istemciye gönderilmez; ayrıntı log'a yazılır.
- **Gerçek veritabanıyla test.** Entegrasyon testleri Testcontainers ile her çalıştırmada boş bir PostgreSQL üzerinde çalışır; Flyway migration'ları da bu sırada doğrulanmış olur.
- **Doğrulama istemcide ve sunucuda.** Form, hatalı girdiyi istek atmadan yakalar; asıl kural sunucudadır. Sunucunun döndüğü alan hataları formda ilgili alanın altında, diğer hatalar (ör. `409`) formun üstünde gösterilir. İstek sürerken gönder butonu kilitlenir.
- **Yapılandırma ortam değişkeniyle.** Aynı Docker imajı her ortamda çalışır; veritabanı adresi imaja gömülmez, `SPRING_DATASOURCE_URL` ile dışarıdan verilir. Backend, PostgreSQL'in healthcheck'i geçmesini bekleyerek başlar. Konteyner root olmayan bir kullanıcıyla çalışır; uygulama kendi jar dosyasını değiştiremez.
- **Protokol çözümü ağ katmanından ayrı.** `PacketCodec` başlığı doğrular ve mesaj tipine göre ilgili çözücüyü seçer; Netty'ye bağımlı değildir ve ağ olmadan birim testiyle doğrulanır. Yeni bir mesaj tipi, yalnızca yeni bir `PayloadDecoder` bileşeni yazılarak eklenir. Bozuk paketler loglanıp atılır, sunucu çalışmaya devam eder.
- **Sıra kontrolü ve yazma tek adımda.** Son ölçüm Redis'te sensör başına bir hash olarak tutulur. Sıra numarası karşılaştırması, yazma ve TTL bir Lua script'iyle Redis içinde atomik olarak yapılır; böylece eski, tekrar eden ya da sırası karışmış paketler, birden fazla servis örneğinde bile güncel değerin üzerine yazamaz. 10 saniyelik TTL hem "sessiz sensör" durumunu hem de yeniden başlayan sensörün kabul edilmesini sağlar.
- **Simülatör sunucudan bağımsız.** Simülatör, sunucunun kodunu kullanmadan yalnızca ICD'ye göre yazılmıştır ve ICD'deki örnek pakete karşı kendini doğrular. Bu sayede iki tarafta aynı yanlış anlama birbirini gizleyemez.
- **CORS yerine proxy.** Geliştirmede arayüz, istekleri Vite proxy üzerinden backend'e iletir; arayüz kodunda backend adresi yer almaz.

## Bilinen sınırlar

- **Redis yazma işlemi Netty event loop thread'inde, senkron yapılıyor.** Yüksek paket hızında ya da Redis yavaşladığında UDP okumayı geciktirir. Etkisi 500 ms komut zaman aşımıyla sınırlandırıldı; Kafka aşamasında ölçümler asenkron olarak Kafka'ya gönderilecek ve Redis yazma işi tüketici tarafına taşınacak.
- **UDP ile gelen `sensorId` doğrulanmıyor.** Veritabanında olmayan ya da pasif bir sensörün verisi de kabul ediliyor.
- **Arayüz son değeri 2 saniyede bir sorgulayarak (polling) alıyor.** WebSocket aşamasında sunucudan itme (push) yöntemine geçilecek.

## Yol haritası

- [x] Sensör CRUD, doğrulama, hata yönetimi
- [x] Birim ve entegrasyon testleri
- [x] React arayüzü: listeleme ve pasifleştirme
- [x] Sensör ekleme formu
- [x] Servisin Docker imajı
- [x] Sensörlerden UDP ile veri alımı (Netty), son durum için Redis
- [ ] Kafka ile olay yayını ve ayrı bir alarm servisi
- [ ] WebSocket ile canlı izleme ekranı
- [ ] Elasticsearch ile sensör olaylarında arama
- [ ] CI (GitHub Actions), SonarQube ile kod kalitesi analizi
- [ ] Kubernetes manifestleri
