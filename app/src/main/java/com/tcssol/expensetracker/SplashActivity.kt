package com.tcssol.expensetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(intent)
            finish()
        } else {
            setContentView(R.layout.splash_screen)
            val runnable = Runnable {
                startActivity(intent)
                finish()
            }
            Handler(Looper.getMainLooper()).postDelayed(runnable, 2000)
        }
    }
}
