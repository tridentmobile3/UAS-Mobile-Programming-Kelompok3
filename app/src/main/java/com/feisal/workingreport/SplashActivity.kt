package com.feisal.workingreport

import androidx.core.view.WindowCompat
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val greetings = arrayOf("Hello","Bonjour","こんにちは","안녕하세요","你好","Padepokan 79")
    private var currentIndex = 0

    // Palet 4 Warna Khas Padepokan 79
    private val padepokanColors = intArrayOf(
        Color.parseColor("#E53935"), // Merah
        Color.parseColor("#4CAF50"), // Hijau
        Color.parseColor("#1E88E5"), // Biru
        Color.parseColor("#FBC02D")  // Kuning
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        setContentView(R.layout.activity_splash)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (currentIndex < greetings.size) {
                    tvGreeting.text = getColorfulText(greetings[currentIndex])
                    tvGreeting.startAnimation(slideUpAnim)

                    currentIndex++
                    handler.postDelayed(this, 380) // Waktu
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }
        }

        handler.post(runnable)
    }

    //untuk mewarnai tiap huruf
    private fun getColorfulText(text: String): SpannableString {
        val spannable = SpannableString(text)
        var colorIndex = 0 // Penghitung warna agar tidak terganggu spasi

        for (i in text.indices) {
            if (text[i] != ' ') { // Abaikan karakter spasi
                val color = padepokanColors[colorIndex % padepokanColors.size]
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    i, i + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                colorIndex++
            }
        }
        return spannable
    }
}