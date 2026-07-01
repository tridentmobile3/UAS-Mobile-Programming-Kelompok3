package com.feisal.workingreport

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LaporanBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var tvUploadHint: TextView
    private var currentTab = "FILE"
    private var selectedFileUri: Uri? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            tvUploadHint.text = "Lampiran berhasil ditambahkan! ✅"
            tvUploadHint.setTextColor(Color.parseColor("#2ECC71"))
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            tvUploadHint.text = "Screenshot berhasil ditambahkan! ✅"
            tvUploadHint.setTextColor(Color.parseColor("#2ECC71"))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_bottom_sheet_laporan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etTanggal = view.findViewById<EditText>(R.id.etTanggalLaporan)
        val etJamMulai = view.findViewById<EditText>(R.id.etJamMulai)
        val etJamSelesai = view.findViewById<EditText>(R.id.etJamSelesai)

        val cal = Calendar.getInstance()

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
                val waktu = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                etJamMulai.setText(waktu)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        etJamSelesai.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                val waktu = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                etJamSelesai.setText(waktu)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        val tabFile = view.findViewById<MaterialCardView>(R.id.tabFileLaporan)
        val tabFoto = view.findViewById<MaterialCardView>(R.id.tabFotoLaporan)
        val tabLink = view.findViewById<MaterialCardView>(R.id.tabLinkLaporan)
        tvUploadHint = view.findViewById(R.id.tvUploadHintLaporan)
        val etLinkDrive = view.findViewById<EditText>(R.id.etLinkDriveLaporan)
        val boxUpload = view.findViewById<LinearLayout>(R.id.boxUploadLaporan)

        fun resetTabs() {
            tabFile.setCardBackgroundColor(Color.parseColor("#222831"))
            tabFoto.setCardBackgroundColor(Color.parseColor("#222831"))
            tabLink.setCardBackgroundColor(Color.parseColor("#222831"))
            tvUploadHint.setTextColor(Color.parseColor("#8B95A5"))
            tvUploadHint.visibility = View.VISIBLE
            etLinkDrive.visibility = View.GONE
            selectedFileUri = null
        }

        tabFile.setOnClickListener {
            resetTabs()
            currentTab = "FILE"
            tabFile.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.text = Html.fromHtml("Tarik file/screenshot di sini atau <font color='#3498DB'>pilih file</font>", Html.FROM_HTML_MODE_LEGACY)
        }

        tabFoto.setOnClickListener {
            resetTabs()
            currentTab = "FOTO"
            tabFoto.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.text = Html.fromHtml("Pilih foto dari Galeri <font color='#3498DB'>di sini</font>", Html.FROM_HTML_MODE_LEGACY)
        }

        tabLink.setOnClickListener {
            resetTabs()
            currentTab = "LINK"
            tabLink.setCardBackgroundColor(Color.parseColor("#1E3A8A"))
            tvUploadHint.visibility = View.GONE
            etLinkDrive.visibility = View.VISIBLE
        }

        tabFile.performClick()

        boxUpload.setOnClickListener {
            when (currentTab) {
                "FILE" -> pickFileLauncher.launch("application/*")
                "FOTO" -> pickImageLauncher.launch("image/*")
                "LINK" -> etLinkDrive.requestFocus()
            }
        }

        val btnKirim = view.findViewById<Button>(R.id.btnKirimLaporan)
        val etJudul = view.findViewById<EditText>(R.id.etJudulLaporan)
        val etDeskripsi = view.findViewById<EditText>(R.id.etDeskripsiLaporan)

        btnKirim.setOnClickListener {
            if (etJudul.text.toString().isEmpty() || etDeskripsi.text.toString().isEmpty()) {
                Toast.makeText(context, "Judul dan Deskripsi harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(context, "Laporan Kerja Terkirim!", Toast.LENGTH_LONG).show()
            dismiss()
        }
    }
}