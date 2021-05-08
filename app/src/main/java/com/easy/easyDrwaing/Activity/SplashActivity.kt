package com.easy.easyDrwaing.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.easy.easyDrwaing.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hiding Status Bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        // Intent to Drawing Activity
        val btnContinue = findViewById<Button>(R.id.btn_continue)
        btnContinue.setOnClickListener {
            val intent = Intent(this, DrawingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}