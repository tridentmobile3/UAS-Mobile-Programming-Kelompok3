# Sapta Work

Sapta Work adalah aplikasi Android Native Kotlin untuk absensi karyawan dan working report pada PT Padepokan Tujuh Sembilan. Proyek ini dibuat untuk memenuhi Tugas Besar mata kuliah **Mobile Programming** di Universitas Teknologi Bandung.

## Fitur Utama

- **Login NIP & Password**: Masuk menggunakan Nomor Induk Pegawai dan kata sandi yang aman.
- **Role-Based Access**: Sistem khusus untuk role HC (Human Capital) dan Karyawan.
- **Absensi Selfie & Wajah**: Check-in aman menggunakan verifikasi wajah/selfie.
- **Geofencing (GPS Lock)**: Absensi hanya dapat dilakukan dalam radius kantor yang ditentukan.
- **Lock Absensi Harian**: Mencegah manipulasi dengan membatasi satu kali absensi per hari.
- **Working Report Harian**: Pelaporan progres kerja, kendala, dan rencana kerja harian.
- **Manajemen Izin/Sakit**: Pengajuan izin dengan bukti dokumen/foto/PDF.
- **Dashboard HC**: Pemantauan real-time absensi, izin, dan laporan kerja seluruh karyawan.

## Teknologi & Arsitektur

- **Bahasa**: Android Native Kotlin
- **Backend**: Firebase Authentication (NIP mapping to Email)
- **Database**: Cloud Firestore
- **Storage**: Firebase Storage (Foto absensi & Bukti izin)
- **Komponen UI**: Activity, Fragment, RecyclerView, Intent, ViewBinding (Admin), Compose (Karyawan)
- **Arsitektur**: Repository Pattern dengan Kotlin Coroutines

## Struktur Proyek

- `app/`: Kode sumber utama Android (setelah integrasi).
- `Backend/absensi/`: (Legacy) Logika repository awal dan model data.
- `docs/`: Dokumentasi teknis dan walkthrough integrasi.
- `apk/`: Lokasi penyimpanan file APK hasil build.

## Cara Menjalankan

1. Clone repositori ini.
2. Pindah ke branch `arga`: `git checkout arga`.
3. Buka folder root menggunakan **Android Studio**.
4. **PENTING**: Masukkan file `google-services.json` asli ke folder `app/`.
5. Pastikan data seed (User & Office Location) sudah tersedia di Firebase Console.
6. Build dan jalankan di perangkat Android atau Emulator.

---

## Tim Pengembang (TIF RP 24 CNS)

| Nama | NIM | Role |
| :--- | :--- | :--- |
| **Dafa Irsyad Nasrullah** | 24552011306 | Frontend Developer |
| **Diky Raihan Subagja** | 24552011194 | UI/UX Designer |
| **Feisal Ramdhani Riyadi** | 24552011317 | Frontend Developer |
| **Muhamad Arga Reksapati** | 24552011324 | Backend Developer |
