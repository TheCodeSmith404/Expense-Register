package com.tcssol.expensetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(intent);
            finish();
        } else {

            setContentView(R.layout.splash_screen);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                    finish();
                }
            };

            Handler handler = new Handler();
            handler.postDelayed(runnable, 2000);
        }
    }
}