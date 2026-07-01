package com.feisal.workingreport

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.feisal.workingreport.repository.AttendanceRepository
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraAbsenActivity : AppCompatActivity() {
    private lateinit var viewFinder: PreviewView
    private var imageCapture: ImageCapture? = null
    private val attendanceRepository = AttendanceRepository()
    private var type: String = "CHECK_IN"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Izin kamera ditolak, tidak bisa absen.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_absen)

        type = intent.getStringExtra("type") ?: "CHECK_IN"
        viewFinder = findViewById(R.id.viewFinder)

        // 1. Logika Tombol Close (X)
        val btnClose = findViewById<ImageView>(R.id.btnClose)
        btnClose.setOnClickListener {
            finish()
        }

        // 2. Logika Tombol Jepret
        val btnCapture = findViewById<MaterialCardView>(R.id.btnCapture)
        btnCapture.setOnClickListener {
            takePhoto()
        }

        // Cek izin kamera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Inisialisasi fitur penangkap gambar
            imageCapture = ImageCapture.Builder().build()

            // Gunakan kamera depan
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                // Hubungkan preview DAN imageCapture ke kamera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Gagal menyalakan kamera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {

        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val photoFile = File(externalCacheDir, "Absen_$name.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@CameraAbsenActivity, "Gagal jepret foto: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    
                    lifecycleScope.launch {
                        val result = if (type == "CHECK_IN") {
                            attendanceRepository.checkIn(
                                latitude = -6.9174639,
                                longitude = 107.6191228,
                                accuracy = 10f,
                                photoUri = savedUri,
                                faceVerified = true
                            )
                        } else {
                            attendanceRepository.checkOut(
                                latitude = -6.9174639,
                                longitude = 107.6191228,
                                accuracy = 10f,
                                photoUri = savedUri
                            )
                        }
                        
                        result.onSuccess {
                            val msg = if (type == "CHECK_IN") "Absen Berhasil Masuk!" else "Absen Berhasil Pulang!"
                            Toast.makeText(this@CameraAbsenActivity, msg, Toast.LENGTH_LONG).show()
                            finish()
                        }.onFailure {
                            Toast.makeText(this@CameraAbsenActivity, "Gagal absen: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        )
    }
}
