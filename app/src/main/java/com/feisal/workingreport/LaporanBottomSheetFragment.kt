package com.feisal.workingreport

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.feisal.workingreport.repository.WorkingReportRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class LaporanBottomSheetFragment : BottomSheetDialogFragment() {

    // Interface callback untuk memperbarui data di LaporanKerjaActivity secara realtime
    interface OnReportSubmittedListener {
        fun onReportSubmitted()
    }

    private var listener: OnReportSubmittedListener? = null

    private lateinit var tvUploadHint: TextView
    private lateinit var tvNamaFileTerpilih: TextView
    private lateinit var etLinkDrive: TextInputEditText

    private var currentTab = "FILE"
    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var selectedMimeType: String? = null

    private val reportRepository = WorkingReportRepository()

    fun setOnReportSubmittedListener(listener: OnReportSubmittedListener) {
        this.listener = listener
    }

    // Ambil Berkas Lewat File Manager
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        handleAttachmentResult(uri, "application/*")
    }

    // Ambil Foto Lewat Galeri / Kamera
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        handleAttachmentResult(uri, "image/*")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_bottom_sheet_laporan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind TextInputLayout (Untuk sistem validasi error teks merah di bawah kolom)
        val tilTanggal = view.findViewById<TextInputLayout>(R.id.tilTanggalLaporan)
        val tilJudul = view.findViewById<TextInputLayout>(R.id.tilJudulLaporan)
        val tilDeskripsi = view.findViewById<TextInputLayout>(R.id.tilDeskripsiLaporan)
        val tilJamMulai = view.findViewById<TextInputLayout>(R.id.tilJamMulai)
        val tilJamSelesai = view.findViewById<TextInputLayout>(R.id.tilJamSelesai)

        // Bind TextInputEditText (Sesuai dengan isi komponen di dalam XML kamu)
        val etTanggal = view.findViewById<TextInputEditText>(R.id.etTanggalLaporan)
        val etJamMulai = view.findViewById<TextInputEditText>(R.id.etJamMulai)
        val etJamSelesai = view.findViewById<TextInputEditText>(R.id.etJamSelesai)
        val etJudul = view.findViewById<TextInputEditText>(R.id.etJudulLaporan)
        val etDeskripsi = view.findViewById<TextInputEditText>(R.id.etDeskripsiLaporan)

        // Bind Komponen Lampiran & Aksi
        val tabFile = view.findViewById<MaterialCardView>(R.id.tabFileLaporan)
        val tabFoto = view.findViewById<MaterialCardView>(R.id.tabFotoLaporan)
        val tabLink = view.findViewById<MaterialCardView>(R.id.tabLinkLaporan)

        tvUploadHint = view.findViewById(R.id.tvUploadHintLaporan)
        tvNamaFileTerpilih = view.findViewById(R.id.tvNamaFileTerpilih)
        etLinkDrive = view.findViewById(R.id.etLinkDriveLaporan)
        val boxUpload = view.findViewById<LinearLayout>(R.id.boxUploadLaporan)
        val btnKirim = view.findViewById<MaterialButton>(R.id.btnKirimLaporan)

        val cal = Calendar.getInstance()

        // 1. DATE & TIME PICKER
        etTanggal.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val formatTanggal = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                etTanggal.setText(formatTanggal.format(cal.time))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        etJamMulai.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                etJamMulai.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        etJamSelesai.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                etJamSelesai.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        // 2. MANAGEMENT SWITCHING TAB LAMPIRAN
        fun resetTabs() {
            tabFile.setCardBackgroundColor(Color.parseColor("#222831"))
            tabFoto.setCardBackgroundColor(Color.parseColor("#222831"))
            tabLink.setCardBackgroundColor(Color.parseColor("#222831"))
            tvUploadHint.setTextColor(Color.parseColor("#8B95A5"))
            tvUploadHint.visibility = View.VISIBLE
            tvNamaFileTerpilih.visibility = View.GONE
            etLinkDrive.visibility = View.GONE
            selectedFileUri = null
            selectedFileName = null
            selectedMimeType = null
        }

        tabFile.setOnClickListener {
            resetTabs()
            currentTab = "FILE"
            tabFile.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.text = Html.fromHtml("Tarik file di sini atau <font color='#3498DB'>pilih file</font>", Html.FROM_HTML_MODE_LEGACY)
        }

        tabFoto.setOnClickListener {
            resetTabs()
            currentTab = "FOTO"
            tabFoto.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.text = Html.fromHtml("Pilih foto/screenshot <font color='#3498DB'>di sini</font>", Html.FROM_HTML_MODE_LEGACY)
        }

        tabLink.setOnClickListener {
            resetTabs()
            currentTab = "LINK"
            tabLink.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.visibility = View.GONE
            etLinkDrive.visibility = View.VISIBLE
        }

        // Trigger tab pertama kali saat dibuka
        tabFile.performClick()

        // 3. LOGIKA KLIK KOTAK UPLOAD (Bisa memilih berkas / membuka berkas terpilih)
        boxUpload.setOnClickListener {
            if (selectedFileUri != null) {
                // Jika file sudah di-upload, klik kotak akan otomatis MEMBUKA file tersebut
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(selectedFileUri, selectedMimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Tidak ada aplikasi yang mendukung untuk membuka file ini", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Jika belum ada file, buka picker sesuai tab aktif
                when (currentTab) {
                    "FILE" -> pickFileLauncher.launch("*/*")
                    "FOTO" -> pickImageLauncher.launch("image/*")
                    "LINK" -> etLinkDrive.requestFocus()
                }
            }
        }

        // 4. VALIDASI LENGKAP & POST DATA DATABASE
        btnKirim.setOnClickListener {
            // Bersihkan sisa status error lama
            tilTanggal?.error = null
            tilJudul?.error = null
            tilDeskripsi?.error = null
            tilJamMulai?.error = null
            tilJamSelesai?.error = null

            val tanggal = etTanggal.text.toString().trim()
            val judul = etJudul.text.toString().trim()
            val deskripsi = etDeskripsi.text.toString().trim()
            val jamMulaiStr = etJamMulai.text.toString().trim()
            val jamSelesaiStr = etJamSelesai.text.toString().trim()
            val linkDriveStr = etLinkDrive.text.toString().trim()

            var isValid = true
            val kolomKosong = mutableListOf<String>()

            // A. Validasi Field Kosong & Penanda Error Spesifik
            if (tanggal.isEmpty()) { kolomKosong.add("Tanggal"); tilTanggal?.error = "Tanggal wajib diisi"; isValid = false }
            if (judul.isEmpty()) { kolomKosong.add("Judul"); tilJudul?.error = "Judul wajib diisi"; isValid = false }
            if (deskripsi.isEmpty()) { kolomKosong.add("Deskripsi"); tilDeskripsi?.error = "Deskripsi wajib diisi"; isValid = false }
            if (jamMulaiStr.isEmpty()) { kolomKosong.add("Jam Mulai"); tilJamMulai?.error = "Wajib diisi"; isValid = false }
            if (jamSelesaiStr.isEmpty()) { kolomKosong.add("Jam Selesai"); tilJamSelesai?.error = "Wajib diisi"; isValid = false }

            if (currentTab == "LINK" && linkDriveStr.isEmpty()) {
                kolomKosong.add("Link Drive")
                etLinkDrive.error = "Link Google Drive tidak boleh kosong!"
                isValid = false
            }

            if (!isValid) {
                Toast.makeText(context, "Field berikut masih kosong: ${kolomKosong.joinToString(", ")}", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // B. Validasi Logika Waktu & Batas Durasi Maksimal 8 Jam
            try {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val dateMulai = sdf.parse(jamMulaiStr)
                val dateSelesai = sdf.parse(jamSelesaiStr)

                if (dateMulai != null && dateSelesai != null) {
                    // Cek jika jam mulai melampaui jam selesai
                    if (dateMulai.after(dateSelesai)) {
                        tilJamMulai?.error = "Jam mulai harus sebelum jam selesai"
                        Toast.makeText(context, "Durasi mulai kerja tidak boleh lebih besar dari durasi selesai!", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    // Cek rentang waktu pengerjaan maksimal 8 jam
                    val diffInMillis = dateSelesai.time - dateMulai.time
                    val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)

                    if (diffInHours > 8) {
                        tilJamSelesai?.error = "Durasi kerja maksimal 8 jam"
                        Toast.makeText(context, "Durasi kerja tidak boleh lebih dari 8 jam! (Saat ini: $diffInHours jam)", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Format penulisan jam salah", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // C. Kirim Data Ke Repository Database
            btnKirim.isEnabled = false
            btnKirim.text = "Mengirim Laporan..."

            lifecycleScope.launch {
                val finalUri = if (currentTab != "LINK") selectedFileUri else null
                val finalFileName = if (currentTab == "LINK") "Google Drive Link" else selectedFileName
                val finalMime = if (currentTab == "LINK") "text/url" else selectedMimeType
                val deskripsiTambahan = if (currentTab == "LINK") "$deskripsi\n\nLink Drive: $linkDriveStr" else deskripsi

                val result = reportRepository.submitReport(
                    startTime = jamMulaiStr,
                    endTime = jamSelesaiStr,
                    workLocation = "WFO",
                    title = judul,
                    description = deskripsiTambahan,
                    progress = "100%",
                    obstacle = "-",
                    nextPlan = "-",
                    attachmentUri = finalUri,
                    fileName = finalFileName,
                    mimeType = finalMime
                )

                result.onSuccess {
                    Toast.makeText(context, "Laporan Kerja Berhasil Dikirim!", Toast.LENGTH_SHORT).show()
                    listener?.onReportSubmitted() // Memicu refresh otomatis di halaman utama activity
                    dismiss()
                }.onFailure { exception ->
                    btnKirim.isEnabled = true
                    btnKirim.text = "Kirim Laporan"
                    Toast.makeText(context, "Gagal menyimpan data: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Fungsi pembantu mengekstrak metadata file asli Android Content Uri
    private fun handleAttachmentResult(uri: Uri?, mimeType: String) {
        if (uri != null) {
            selectedFileUri = uri
            selectedMimeType = mimeType

            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex != -1) {
                    selectedFileName = cursor.getString(nameIndex)
                }
            }

            if (selectedFileName == null) {
                selectedFileName = if (mimeType.contains("image")) "screenshot.jpg" else "file_laporan.dat"
            }

            // Update UI Area Box Lampiran sesuai ID XML kamu
            tvUploadHint.text = "Berkas berhasil dimuat! (Klik kotak untuk membuka berkas) ✅"
            tvUploadHint.setTextColor(Color.parseColor("#2ECC71"))

            tvNamaFileTerpilih.text = selectedFileName
            tvNamaFileTerpilih.visibility = View.VISIBLE
        }
    }
}