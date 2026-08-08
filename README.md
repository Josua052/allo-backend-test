# Split Bill API - Allo Bank Backend Developer Take-Home Test

Repositori ini berisi solusi saya untuk take-home test Backend Developer Allo Bank: sebuah REST API untuk split bill (pembagian tagihan) yang dibangun dengan Spring Boot.

## Tentang Proyek Ini

Selain memenuhi requirement dasar dari soal test, saya menambahkan beberapa hal yang menurut saya penting untuk aplikasi finansial:

- **Algoritma settlement dengan pendekatan greedy** dimana  meminimalkan jumlah transaksi transfer antar anggota grup dengan memasangkan pengutang terbesar dengan pihak yang paling banyak diutangi.
- **Dua strategi split**: `EXACT` (nominal tetap) dan `PERCENTAGE` (persentase), keduanya bisa dipakai bergantian dalam satu grup.
- **Penanganan pembulatan pada split persentase**, agar total split tidak meleset satu rupiah pun dari total tagihan akibat pembulatan desimal.
- **Strategy Pattern + Spring auto-discovery** untuk logika split, agar menambah metode split baru di masa depan tidak perlu mengubah kode service yang sudah ada.
- **Global exception handler**, agar stack trace internal tidak bocor ke response API saat terjadi error.

## Cara Menjalankan

Ada dua cara untuk menjalankan aplikasi ini: pakai Docker, atau langsung lewat Maven Wrapper.

### Docker (disarankan)

Dockerfile sudah pakai multi-stage build agar image hasil build lebih kecil dan tidak perlu install Java di komputer.

1. Dari root folder repo, build image-nya:
   ```bash
   docker build -t split-bill-api .
   ```
2. Jalankan container:
   ```bash
   docker run -p 4110:4110 split-bill-api
   ```
3. API bisa diakses di `http://localhost:4110`.

### Maven Wrapper (lokal)

Kalau mau lihat langsung kodenya atau jalankan test, pastikan Java 17+ sudah terpasang.

1. Jalankan unit test dan integration test:
   ```bash
   ./mvnw test
   ```
2. Jalankan aplikasinya:
   ```bash
   ./mvnw spring-boot:run
   ```

## 💻 Panduan Penggunaan API (*cURL Examples*)

Untuk memudahkan pengujian, aplikasi ini telah dilengkapi dengan antarmuka grafis interaktif berbasis **Swagger UI (OpenAPI 3)**.
Anda dapat langsung mengaksesnya melalui *browser* setelah server berjalan:
👉 **[http://localhost:4110/swagger-ui/index.html](http://localhost:4110/swagger-ui/index.html)**

Jika Anda lebih menyukai terminal, berikut adalah perintah yang dapat langsung Anda *Copy-Paste* untuk menguji aplikasi secara runut.

### 1. Membuat grup baru

Contoh: grup "Trip to Bali" dengan dua anggota, Alice dan Bob.

```bash
curl -X POST http://localhost:4110/api/v1/groups \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Trip to Bali",
    "participantNames": ["Alice", "Bob"]
  }'
```

Simpan `groupId` dan ID masing-masing partisipan dari response, karena dibutuhkan di langkah berikutnya.

### 2. Mencatat tagihan dengan strategi EXACT

Alice bayar tiket pesawat Rp 100.000, dibagi rata Rp 50.000 untuk masing-masing.

```bash
curl -X POST http://localhost:4110/api/v1/groups/{groupId}/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidByParticipantId": "{id_alice}",
    "amount": 100000,
    "description": "Flight Tickets",
    "splitStrategy": "EXACT",
    "splits": [
      { "participantId": "{id_alice}", "amountOwed": 50000 },
      { "participantId": "{id_bob}", "amountOwed": 50000 }
    ]
  }'
```

### 3. Mencatat tagihan dengan strategi PERCENTAGE

Bob traktir makan siang Rp 60.000, dibagi 60% untuk Bob dan 40% untuk Alice. Untuk strategi ini, field `amountOwed` diisi persentase, bukan nominal.

```bash
curl -X POST http://localhost:4110/api/v1/groups/{groupId}/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "paidByParticipantId": "{id_bob}",
    "amount": 60000,
    "description": "Lunch at Beach",
    "splitStrategy": "PERCENTAGE",
    "splits": [
      { "participantId": "{id_bob}", "amountOwed": 60 },
      { "participantId": "{id_alice}", "amountOwed": 40 }
    ]
  }'
```

### 4. Melihat ringkasan settlement

Endpoint ini menghitung siapa perlu transfer ke siapa agar semua orang impas.

```bash
curl -X GET http://localhost:4110/api/v1/groups/{groupId}/settlements
```

## Catatan: Perhitungan Service Charge

Sesuai instruksi test, service charge dihitung dari nilai ASCII username GitHub saya (huruf kecil semua):

- Username: `josua052`
- Total nilai ASCII: `j(106) + o(111) + s(115) + u(117) + a(97) + 0(48) + 5(53) + 2(50) = 697`
- Service charge: `697 % 10 = 7`, sehingga dipotong 7% dari total pengeluaran grup di setiap perhitungan settlement.

## Pertanyaan Submission: Keputusan Desain Tersulit

**"What was the hardest design decision you made while building this, and what trade-off did you accept?"**

Bagian tersulit adalah menentukan cara menangani beberapa jenis strategi pembagian tagihan (EXACT dan PERCENTAGE). Cara paling cepat sebenarnya cukup taruh semua logikanya dalam satu blok if-else besar di `ExpenseService`, dan itu akan tetap lolos dari sisi fungsional.

Tapi saya memilih untuk memisahkan logika ini pakai Strategy Pattern dengan masing-masing strategi (`ExactSplitStrategy`, `PercentageSplitStrategy`) jadi class terpisah yang di-inject otomatis oleh Spring. Pendekatan ini juga yang saya pakai untuk menyelesaikan masalah pembulatan (sisa satu rupiah yang sering muncul saat membagi tagihan berbasis persentase), agar logika pembulatan ini terisolasi dan tidak mengotori business logic utama.

Trade-off-nya: struktur project jadi punya lebih banyak file dan sedikit lebih kompleks di awal dibanding pendekatan if-else. Tapi saya anggap ini worth it, karena kalau nanti perlu tambah strategi split baru (misalnya split berdasarkan share/weight), saya tinggal tambah satu class baru tanpa perlu menyentuh kode service yang sudah ada dan sudah teruji.