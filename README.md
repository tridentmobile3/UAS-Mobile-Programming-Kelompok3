# Sapta Work

Sapta Work adalah aplikasi Android Native Kotlin untuk absensi karyawan dan working report. Repository ini dirapikan untuk kebutuhan pengumpulan UAS Mobile Programming Kelompok 3 tanpa mengubah package utama aplikasi, sehingga tetap aman untuk build dan demo.

## Kesimpulan Ketentuan UAS

Fokus penilaian dosen untuk project ini adalah:
- Aplikasi Android Native Kotlin, bukan Flutter atau React Native
- Aplikasi tidak force close atau crash saat dijalankan
- Fitur inti tetap berjalan sesuai tujuan awal project
- Repository GitHub berisi source code Android Studio, APK, link video demo di README, dan laporan OOAD di folder `docs/`

Karena itu, package utama tetap menggunakan `com.feisal.workingreport` dan perapihan dilakukan secukupnya tanpa refactor besar yang berisiko merusak aplikasi.

## Struktur Repository Final

```text
UAS-Mobile-Programming-Kelompok3/
|-- app/
|-- apk/
|   |-- sapta-work-debug.apk
|   `-- sapta-work-release.apk
|-- docs/
|   |-- Laporan_OOAD_Sapta_Work.pdf
|   `-- screenshots/
|       |-- login.jpg
|       |-- dashboard-karyawan.jpg
|       `-- dashboard-admin.jpg
|-- README.md
|-- build.gradle.kts
|-- settings.gradle.kts
`-- gradle/
```

## Checklist Submission UAS

- [x] Folder `app` tersedia
- [x] Folder `apk` tersedia
- [x] APK debug dan release tersedia di folder `apk`
- [x] Folder `docs` tersedia
- [x] PDF laporan OOAD tersedia di `docs/`
- [x] README sudah dilengkapi
- [ ] Link video demo final masih perlu diganti
- [x] Screenshot minimal 2 sudah dimasukkan ke README

## Anggota Kelompok

| Nama | NIM | Peran |
|---|---|---|
| Muhamad Arga Reksapati | 24552011324 | Backend / Firebase Integration |
| Feisal Ramdhani Riyadi | 24552011317 | UI / Frontend |
| Diky Raihan Subagja | 24552011194 | Admin Page / UI Support |
| Dafa Irsyad Nasrullah | 24552011306 | Testing / Documentation |

## Fitur

- Login NIP dan password
- Role HC dan Karyawan
- Absensi masuk dan pulang menggunakan kamera
- Validasi lokasi kantor
- Riwayat absensi
- Pengajuan izin
- Working report dengan lampiran data kerja
- Pengajuan dan riwayat lembur
- Dashboard admin HC
- Notifikasi aktivitas login

## Teknologi

- Android Native Kotlin
- Firebase Authentication
- Cloud Firestore
- Firebase Storage
- CameraX
- Activity, Intent, Fragment, Jetpack Compose, ViewBinding

## Struktur Kode Aman untuk UAS

Package utama tetap:

```text
com.feisal.workingreport
```

Perapihan dilakukan dari dalam module `app/`, terutama pada folder `model/`, `repository/`, `service/`, `ui/`, dan `utils/`, tanpa memindahkan seluruh Activity atau mengubah arsitektur besar-besaran.

## Screenshot Tampilan

### Login

![Login](docs/screenshots/login.jpg)

### Dashboard Karyawan

![Dashboard Karyawan](docs/screenshots/dashboard-karyawan.jpg)

### Dashboard Admin / HC

![Dashboard Admin](docs/screenshots/dashboard-admin.jpg)

## APK

- Debug: [apk/sapta-work-debug.apk](apk/sapta-work-debug.apk)
- Release: [apk/sapta-work-release.apk](apk/sapta-work-release.apk)

## Laporan OOAD

File laporan: [docs/Laporan_OOAD_Sapta_Work.pdf](docs/Laporan_OOAD_Sapta_Work.pdf)

Catatan: file PDF yang ada saat ini masih placeholder struktur. Ganti file tersebut dengan laporan OOAD final sebelum branch final dikumpulkan.

## Video Demo

Link video demo: `https://youtu.be/REPLACE_WITH_FINAL_DEMO`

Video final wajib memuat:
- Perkenalan anggota kelompok
- Demo fitur inti aplikasi
- Penjelasan singkat alur kode atau komponen utama seperti CRUD, repository, atau tampilan data

## Cara Menjalankan

```bash
git clone https://github.com/tridentmobile3/UAS-Mobile-Programming-Kelompok3.git
cd UAS-Mobile-Programming-Kelompok3
```

Buka project dengan Android Studio, lakukan sync Gradle, lalu jalankan aplikasi di perangkat Android atau emulator. Pastikan izin kamera dan lokasi diberikan saat pengujian fitur absensi.

## Build APK

```bash
gradlew.bat clean :app:assembleDebug
copy app\build\outputs\apk\debug\app-debug.apk apk\sapta-work-debug.apk

gradlew.bat :app:assembleRelease
```

## Catatan Penting

Yang sengaja tidak dilakukan pada tahap ini:
- Rename package utama
- Memindahkan semua Activity ke struktur baru
- Mengubah total arsitektur ke MVVM penuh
- Menghapus fitur yang sudah berjalan

Target repo final adalah aman untuk build, aman untuk demo, dan lengkap untuk pengumpulan UAS.
