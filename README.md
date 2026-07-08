# UAS MOBILE PROGRAMMING KELOMPOK3 📱

[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS-blue)](https://developer.android.com)
[![Framework](https://img.shields.io/badge/Framework-Flutter%20%2F%20React%20Native%20%2F%20Kotlin-green)](#)

## 📝 Penjelasan Aplikasi

**SaptaWork** adalah aplikasi sistem informasi manajemen SDM (Human Capital) berbasis **Android Native** yang dirancang khusus untuk mengoptimalkan efisiensi pencatatan kehadiran, pelaporan kinerja, dan transparansi birokrasi internal perusahaan atau organisasi. 

Aplikasi ini mengadopsi arsitektur modern Android untuk menyediakan dua hak akses (*multi-role*) yang terintegrasi secara *real-time*:

1. **Sisi Karyawan (Mobile Mobility):** Berfungsi sebagai alat utilitas mandiri (*self-service*) bagi karyawan untuk melakukan presensi, melaporkan aktivitas kerja harian, hingga mengajukan lembur dan izin secara digital tanpa perlu form fisik.
2. **Sisi Human Capital (HC/Admin Dashboard):** Berfungsi sebagai pusat kendali (*command center*) bagi manajemen untuk memantau produktivitas tim, menganalisis statistik kehadiran, serta melakukan validasi (*approval*) keputusan secara cepat dan akurat.

Aplikasi ini dikembangkan untuk memenuhi Tugas Besar mata kuliah **Mobile Programming** di **Universitas Teknologi Bandung**, dengan fokus implementasi performa optimal, manajemen memori yang efisien, dan antarmuka ramah pengguna (*User-Friendly*) sesuai standar panduan Material Design dari Google.
### Fitur Utama:

#### 🏢 Human Capital (HC / Admin)
* 📊 **Monitoring Presensi & Statistik**: Memantau grafik kehadiran, status pengajuan, dan ringkasan data statistik sistem secara *real-time*.
* ⚖️ **Approval Sistem**: Melakukan validasi, persetujuan, atau penolakan terhadap pengajuan izin, laporan kerja harian, dan lembur yang diajukan oleh karyawan.
* 👥 **Manajemen Karyawan**: Memiliki hak akses penuh untuk melihat dan mengelola seluruh fitur penunjang operasional yang dimiliki oleh akun karyawan.

#### 👤 Karyawan
* 📍 **Absen (Presensi)**: Melakukan absensi masuk dan pulang menggunakan fitur berbasis GPS/Geofencing atau swafoto.
* 📝 **Ajukan Izin**: Mengisi formulir digital untuk permohonan izin atau sakit lengkap dengan unggah dokumen pendukung (surat dokter/keterangan).
* 📋 **Laporan Kerja**: Melaporkan log aktivitas dan hasil pekerjaan harian sebagai bukti akuntabilitas kinerja.
* ⏳ **Riwayat Presensi**: Mengakses log historis kehadiran mandiri untuk memantau akumulasi jam kerja dan kehadiran bulanan.
* 🌙 **Lembur**: Mengajukan jam kerja lembur beserta deskripsi tugas yang dikerjakan di luar jam kerja reguler.
* 👤 **Profile**: Mengelola data diri, foto profil, informasi akun, dan melihat ringkasan jabatan atau divisi karyawan.
---

## 🛠️ Teknologi & Arsitektur
* **Frontend**: **Kotlin** **Jetpack Compose** **Material Design 3**
* **State Management**: **Kotlin Coroutines**
* **Backend/Database**: **Firebase**
* **Tools**: **Android Studio** **Gradle** **Git** & **GitHub**

---

## 👥 Anggota Kelompok & Pembagian Tugas
Berikut adalah anggota **Kelompok [Nomor Kelompok]** dari kelas **TIF RP 24 CNS**:

| Foto  | Nama & NIM | Role / Jobdesk Utama | GitHub |
| :---: | :--- | :--- | :---: |
| <img src="https://github.com/identicons/dafa1922.png" width="50"> | **Dafa Irsyad Nasrullah** <br> NIM: 24552011306 | • <br>• <br>• | [@dafa1922](https://github.com/dafa1922) |
| <img src="https://github.com/identicons/raihanzdiky.png" width="50"> | **Diky Raihan Subagja** <br> NIM: 24552011194 | •  <br>• <br>•  | [@raihanzdiky](https://github.com/raihanzdiky) |
| <img src="https://github.com/identicons/feifeis17.png" width="50"> | **Feisal Ramdhani Riyadi** <br> NIM: 24552011317 | • <br>• <br>•  | [@feifeis17](https://github.com/feifeis17) |
| <img src="https://github.com/identicons/areksaxyz.png" width="50"> | **Muhamad Arga Reksapati** <br> NIM: 24552011324 | • <br>• <br>•  | [@argareksapati](https://github.com/areksaxyz) |

---

## 🚀 Cara Menjalankan Proyek (Setup)

Prasyarat: Pastikan sudah menginstal [Flutter SDK / React Native CLI / Android SDK] di laptop masing-masing.

1. **Clone Repositori**
```bash
   git clone [https://github.com/tridentmobile3/UAS-Mobile-Programming-Kelompok3](https://github.com/tridentmobile3/UAS-Mobile-Programming-Kelompok3)
   cd UAS-Mobile-Programming-Kelompok3
