# Sensör UDP Protokolü (ICD)

Sensörlerin ölçümlerini sensor-service'e gönderdiği ikili (binary) mesaj formatı.

| | |
|---|---|
| Taşıma | UDP, her datagram tek bir mesaj taşır |
| Bayt sırası | Big-endian (ağ bayt sırası) |
| Protokol sürümü | 1 |
| Tipler | `uintN` işaretsiz, `intN` işaretli tam sayı; `float64` IEEE 754 çift duyarlıklı |

## 1. Başlık (15 bayt)

Tüm mesaj tiplerinde ortaktır.

| Konum | Alan | Tip | Bayt | Açıklama |
|---|---|---|---|---|
| 0 | `magic` | uint16 | 2 | Sabit `0x5346` ("SF"). Pakete ait olmayan trafiği ayıklamak için. |
| 2 | `version` | uint8 | 1 | Protokol sürümü. Şu an `1`. |
| 3 | `messageType` | uint16 | 2 | Mesaj tipi (bkz. bölüm 2). |
| 5 | `payloadLength` | uint16 | 2 | Gövdenin bayt cinsinden uzunluğu. |
| 7 | `sensorId` | uint32 | 4 | Gönderen sensörün kimliği. |
| 11 | `sequence` | uint32 | 4 | Sensör başına artan sıra numarası. |

## 2. Mesaj tipleri

| Tip | Ad | Gövde boyutu | Durum |
|---|---|---|---|
| `1` | Ölçüm (Measurement) | 16 bayt | Tanımlı |
| `2` | Heartbeat | | Planlanıyor |

## 3. Ölçüm mesajı gövdesi (tip 1, 16 bayt)

| Konum | Alan | Tip | Bayt | Açıklama |
|---|---|---|---|---|
| 15 | `timestamp` | int64 | 8 | Ölçümün **alındığı** an; Unix epoch'tan bu yana milisaniye (UTC). Paketin gönderildiği ya da alındığı an değildir. |
| 23 | `value` | float64 | 8 | Ölçülen değer. Birim, sensörün kayıtlı tipinden bellidir. `NaN` ve `Infinity` geçerli değerlerdir (bkz. bölüm 5). |

Toplam paket boyutu: **31 bayt**.

### Örnek

Sensör 42, sıra numarası 7, 2026-01-01T00:00:00Z anında 21.5 değeri:

```
53 46 | 01 | 00 01 | 00 10 | 00 00 00 2A | 00 00 00 07 | 00 00 01 9B 76 DA A8 00 | 40 35 80 00 00 00 00 00
magic  ver  tip     uzunluk  sensorId      sequence      timestamp                 value
```

## 4. Doğrulama kuralları

Alıcı aşağıdaki durumlarda paketi **reddeder**: paket işlenmez, bir uyarı loglanır, alıcı çalışmaya devam eder.

| Kural | Örnek hata mesajı |
|---|---|
| Paket 15 bayttan kısa | `Packet too short: 9 bytes` |
| `magic` ≠ `0x5346` | `Bad magic: 0x1234 (expected 0x5346)` |
| `version` desteklenmiyor | `Unsupported version: 2 (expected 1)` |
| `messageType` tanımsız | `Unknown message type: 7` |
| `payloadLength`, tipin gövde boyutuyla uyuşmuyor | `Payload length mismatch for type 1: 20 (expected 16)` |
| Başlıktan sonra kalan bayt sayısı ≠ `payloadLength` (eksik ya da fazla bayt) | `Packet size mismatch: 17 payload bytes received (header says 16)` |

## 5. Alıcının davranışı

- **Sıra numarası:** Alıcı her sensör için son kabul ettiği `sequence` değerini tutar. Bu değerden küçük ya da ona eşit numaralı paketler eski kabul edilir ve atılır.
- **Sensörün yeniden başlaması:** Bir sensörden **10 saniye** boyunca paket gelmezse, gelen ilk paket sıra numarasına bakılmadan kabul edilir ve sayaç sıfırlanır. Bu nedenle 10 saniyeden kısa sürede yeniden başlayan bir sensörün paketleri, sessizlik süresi dolana kadar atılabilir.
- **`NaN` / `Infinity`:** Alıcı bu değerleri reddetmez. Anlamları (ör. "ölçüm yapılamadı") sensöre özgüdür; alarm ve sunum katmanları bu değerleri ayrıca ele alır.
- **Sıra numarasının taşması:** `uint32`, saniyede bir mesajda yaklaşık 136 yılda başa döner; taşma ele alınmaz.

## 6. Sürüm politikası

- Formatı değiştiren her değişiklik (alan ekleme, çıkarma, boyut değişikliği) `version` değerini artırır.
- Yeni bir mesaj tipi eklemek sürüm değiştirmez; tanımayan alıcılar yeni tipi `Unknown message type` olarak reddeder.
