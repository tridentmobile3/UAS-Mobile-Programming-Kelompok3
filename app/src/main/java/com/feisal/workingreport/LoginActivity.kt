package com.feisal.workingreport

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val mainContainer = findViewById<LinearLayout>(R.id.mainContainer)
        val animSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)
        mainContainer?.startAnimation(animSlideUp)

        applyGradientToTujuhSembilan()
    }

    private fun applyGradientToTujuhSembilan() {
        val tvTujuhSembilan = findViewById<TextView>(R.id.tvTujuhSembilan)
        tvTujuhSembilan.post {
            val width = tvTujuhSembilan.width.toFloat()
            if (width <= 0f) return@post

            val shader = LinearGradient(
                0f, 0f, width, 0f,                 // horizontal, kiri ke kanan
                intArrayOf(
                    Color.parseColor("#1E88E5"),   // biru
                    Color.parseColor("#26C281")    // hijau
                ),
                null,
                Shader.TileMode.CLAMP
            )

            tvTujuhSembilan.paint.shader = shader
            tvTujuhSembilan.invalidate()
        }
    }
}