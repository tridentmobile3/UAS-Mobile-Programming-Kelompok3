package com.feisal.workingreport

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IzinBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var tvUploadHint: TextView
    private lateinit var etLinkDrive: EditText
    private lateinit var etKeterangan: EditText
    private lateinit var spinner: Spinner

    private var currentTab = "FILE"
    private var selectedFileUri: Uri? = null
    private var attachmentBytes: ByteArray? = null
    private var fileExtension = "tmp"

    var onSubmitCallback: ((type: String, reason: String, date: String, bytes: ByteArray?, driveLink: String, ext: String) -> Unit)? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        handleAttachmentResult(uri, "FILE")
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        handleAttachmentResult(uri, "FOTO")
    }

    private fun getBytesFromUri(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val byteBuffer = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    byteBuffer.write(buffer, 0, len)
                }
                byteBuffer.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun handleAttachmentResult(uri: Uri?, tabType: String) {
        val safeContext = context ?: return
        if (uri != null) {
            val bytes = getBytesFromUri(safeContext, uri)
            if (bytes != null) {
                selectedFileUri = uri
                attachmentBytes = bytes

                // Perbaikan parameter getType yang aman
                val mimeType = safeContext.contentResolver.getType(uri)
                fileExtension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                    ?: if (tabType == "FILE") "pdf" else "jpg"

                val labelText = if (tabType == "FILE") "File Dokumen" else "Foto Bukti"
                tvUploadHint.text = Html.fromHtml(
                    "$labelText berhasil dilampirkan! ✅<br><font color='#3498DB'><b>Klik di sini untuk melihat isi file</b></font>",
                    Html.FROM_HTML_MODE_LEGACY
                )
                tvUploadHint.setTextColor(Color.parseColor("#2ECC71"))
            } else {
                Toast.makeText(safeContext, "Gagal membaca data file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(safeContext, "Batal mengambil file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_bottom_sheet_izin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner = view.findViewById(R.id.spinnerJenisIzin)
        val jenisIzin = arrayOf("IZIN", "SAKIT", "CUTI")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, jenisIzin)
        spinner.adapter = adapter

        val tabFile = view.findViewById<MaterialCardView>(R.id.tabFile)
        val tabFoto = view.findViewById<MaterialCardView>(R.id.tabFoto)
        val tabLink = view.findViewById<MaterialCardView>(R.id.tabLink)

        tvUploadHint = view.findViewById(R.id.tvUploadHint)
        etLinkDrive = view.findViewById(R.id.etLinkDrive)
        etKeterangan = view.findViewById(R.id.etKeterangan)
        val boxUpload = view.findViewById<LinearLayout>(R.id.boxUpload)
        val btnKirim = view.findViewById<Button>(R.id.btnKirimPengajuan)

        fun resetTabs() {
            tabFile.setCardBackgroundColor(Color.parseColor("#222831"))
            tabFoto.setCardBackgroundColor(Color.parseColor("#222831"))
            tabLink.setCardBackgroundColor(Color.parseColor("#222831"))
            tvUploadHint.setTextColor(Color.parseColor("#8B95A5"))
            tvUploadHint.visibility = View.VISIBLE
            etLinkDrive.visibility = View.GONE
            selectedFileUri = null
            attachmentBytes = null
        }

        tabFile.setOnClickListener {
            resetTabs()
            currentTab = "FILE"
            tabFile.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.text = Html.fromHtml("Pilih dokumen PDF/DOC <font color='#3498DB'>di sini</font>", Html.FROM_HTML_MODE_LEGACY)
        }

        tabFoto.setOnClickListener {
            resetTabs()
            currentTab = "FOTO"
            tabFoto.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.text = Html.fromHtml("Pilih foto dari Kamera/Galeri <font color='#3498DB'>di sini</font>", Html.FROM_HTML_MODE_LEGACY)
        }

        tabLink.setOnClickListener {
            resetTabs()
            currentTab = "LINK"
            tabLink.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.visibility = View.GONE
            etLinkDrive.visibility = View.VISIBLE
            etLinkDrive.requestFocus()
        }

        tabFile.performClick()

        // Perbaikan Smart Cast Berantai Menggunakan '.let' lokal
        boxUpload.setOnClickListener {
            val currentUri = selectedFileUri
            if (currentUri != null && currentTab != "LINK") {
                try {
                    val mimeType = requireContext().contentResolver.getType(currentUri) ?: "*/*"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(currentUri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(intent, "Buka lampiran dengan:"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Tidak dapat membuka berkas penampil", Toast.LENGTH_SHORT).show()
                }
            } else {
                when (currentTab) {
                    "FILE" -> pickFileLauncher.launch("*/*")
                    "FOTO" -> pickImageLauncher.launch("image/*")
                    "LINK" -> etLinkDrive.requestFocus()
                }
            }
        }

        btnKirim.setOnClickListener {
            val keterangan = etKeterangan.text.toString().trim()
            val selectedType = spinner.selectedItem.toString()
            val driveLink = etLinkDrive.text.toString().trim()
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

            if (keterangan.isEmpty()) {
                Toast.makeText(context, "❌ Keterangan Alasan masih kosong!", Toast.LENGTH_SHORT).show()
                etKeterangan.error = "Harus diisi"
                return@setOnClickListener
            }

            if ((currentTab == "FILE" || currentTab == "FOTO") && attachmentBytes == null) {
                Toast.makeText(context, "❌ Bukti fisik dokumen/foto belum dilampirkan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentTab == "LINK" && driveLink.isEmpty()) {
                Toast.makeText(context, "❌ Link Google Drive masih kosong!", Toast.LENGTH_SHORT).show()
                etLinkDrive.error = "Harus diisi"
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Pengajuan")
                .setMessage("Apakah Anda yakin data yang diisi sudah benar dan ingin mengirim pengajuan izin ini?")
                .setPositiveButton("Ya, Kirim") { dialog, _ ->
                    onSubmitCallback?.invoke(selectedType, keterangan, currentDate, attachmentBytes, driveLink, fileExtension)
                    dialog.dismiss()
                    dismiss()
                }
                .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
}