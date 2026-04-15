# Jadwalku - Sistem Manajemen Agenda Berbasis Android

### Deskripsi Proyek
Jadwalku adalah aplikasi perangkat bergerak (mobile) yang dirancang untuk memfasilitasi manajemen jadwal akademik dan tugas harian secara terintegrasi. Aplikasi ini dibangun menggunakan bahasa pemrograman Kotlin secara asli (native) pada platform Android. Fokus utama dari pengembangan proyek ini adalah efisiensi input data serta sinkronisasi antara penyimpanan lokal dan layanan eksternal.

### Fitur Utama
Sistem ini mengimplementasikan beberapa modul fungsional utama sebagai berikut:

1. **Manajemen Jadwal Kegiatan**: Memungkinkan pengguna untuk melakukan input nama kegiatan, tanggal, serta durasi waktu (waktu mulai dan waktu selesai).
2. **Manajemen Tugas**: Menyediakan formulir untuk pencatatan judul tugas, deskripsi teknis, serta tenggat waktu (deadline).
3. **Sinkronisasi Kalender Eksternal**: Menggunakan protokol Android Intent untuk melakukan injeksi data secara otomatis ke Google Calendar guna aktivasi fitur pengingat sistemik.
4. **Penyimpanan Data Lokal (SQLite)**: Seluruh input pengguna diarsipkan secara permanen ke dalam basis data lokal untuk memastikan aksesibilitas data tanpa ketergantungan koneksi internet.
5. **Sistem Pengingat dan Notifikasi**: Modul khusus untuk meninjau riwayat agenda yang tersimpan serta penyediaan log notifikasi sebagai pusat informasi tenggat waktu bagi pengguna.

### Arsitektur Teknis
Proyek ini mengadopsi standar pengembangan Android modern dengan spesifikasi teknis:
* **Bahasa Pemrograman**: Kotlin.
* **Komponen UI**: XML Layout dengan implementasi Material Design (CardView, LinearLayout, ScrollView).
* **Mesin Basis Data**: SQLite (melalui kelas `SQLiteOpenHelper`).
* **Integrasi Sistem**: Android Calendar Provider API.
* **Logika Relasional**: Penggunaan `User ID` sebagai kunci asing (foreign key) dalam setiap entri data untuk mendukung integritas struktur tabel.

### Skema Basis Data
Basis data `Jadwalku.db` terdiri dari dua tabel utama dengan relasi sebagai berikut:

**Tabel: Jadwal**
* `id` (INTEGER, Primary Key, Autoincrement)
* `user_id` (INTEGER)
* `nama_kegiatan` (TEXT)
* `tanggal` (TEXT)
* `waktu_mulai` (TEXT)
* `waktu_selesai` (TEXT)

**Tabel: Tugas**
* `id` (INTEGER, Primary Key, Autoincrement)
* `user_id` (INTEGER)
* `judul` (TEXT)
* `deskripsi` (TEXT)
* `tanggal_deadline` (TEXT)
* `waktu_deadline` (TEXT)

### Prosedur Instalasi dan Build
Untuk melakukan kompilasi dan instalasi aplikasi ini dari kode sumber, ikuti langkah-langkah berikut:
1. Kloning repositori ini ke direktori lokal.
2. Buka proyek menggunakan perangkat lunak Android Studio (versi Flamingo atau yang lebih baru direkomendasikan).
3. Lakukan sinkronisasi Gradle untuk mengunduh dependensi yang diperlukan.
4. Gunakan menu `Build > Build APK(s)` untuk menghasilkan file executable `.apk`.
5. Instal file `app-debug.apk` pada perangkat Android dengan mengaktifkan izin instalasi dari sumber tidak dikenal.

### Informasi Pengembang
Proyek ini dikembangkan oleh Ramdan Olii, Anggriyani Akuna dan Anggun Rahman dalam rangka pengembangan solusi digital untuk manajemen waktu akademik. Fokus pengembangan saat ini mencakup optimalisasi kode dan pemeliharaan integritas database lokal.
