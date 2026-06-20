package com.feisal.workingreport

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.card.MaterialCardView

class IzinBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var tvUploadHint: TextView
    private var currentTab = "FILE"
    private var selectedFileUri: Uri? = null

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            tvUploadHint.text = "File berhasil dilampirkan! ✅"
            tvUploadHint.setTextColor(Color.parseColor("#2ECC71"))
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            tvUploadHint.text = "Foto berhasil dilampirkan! ✅"
            tvUploadHint.setTextColor(Color.parseColor("#2ECC71"))
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

        val spinner = view.findViewById<Spinner>(R.id.spinnerJenisIzin)
        val jenisIzin = arrayOf("Cuti", "Sakit", "Keperluan Pribadi", "Lainnya")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, jenisIzin)
        spinner.adapter = adapter

        val tabFile = view.findViewById<MaterialCardView>(R.id.tabFile)
        val tabFoto = view.findViewById<MaterialCardView>(R.id.tabFoto)
        val tabLink = view.findViewById<MaterialCardView>(R.id.tabLink)
        tvUploadHint = view.findViewById(R.id.tvUploadHint)

        val etLinkDrive = view.findViewById<EditText>(R.id.etLinkDrive)
        val boxUpload = view.findViewById<LinearLayout>(R.id.boxUpload)

        fun resetTabs() {
            tabFile.setCardBackgroundColor(Color.parseColor("#222831"))
            tabFoto.setCardBackgroundColor(Color.parseColor("#222831"))
            tabLink.setCardBackgroundColor(Color.parseColor("#222831"))

            tvUploadHint.setTextColor(Color.parseColor("#8B95A5")) // Reset teks jadi abu-abu
            tvUploadHint.visibility = View.VISIBLE
            etLinkDrive.visibility = View.GONE
            selectedFileUri = null
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
            tvUploadHint.text = Html.fromHtml("Pilih foto bukti dari Galeri <font color='#3498DB'>di sini</font>", Html.FROM_HTML_MODE_LEGACY)
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
                "LINK" -> {
                    etLinkDrive.requestFocus()
                }
            }
        }
        val btnKirim = view.findViewById<Button>(R.id.btnKirimPengajuan)
        val etKeterangan = view.findViewById<EditText>(R.id.etKeterangan)

        btnKirim.setOnClickListener {
            val keterangan = etKeterangan.text.toString()

            if (keterangan.isEmpty()) {
                Toast.makeText(context, "Keterangan alasan izin tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(context, "Pengajuan Izin Berhasil Dikirim!", Toast.LENGTH_LONG).show()
            dismiss()
        }
    }
}