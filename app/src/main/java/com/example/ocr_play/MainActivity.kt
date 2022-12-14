package com.example.ocr_play

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val leftCapture: Button = findViewById<Button>(R.id.leftCapture)
        leftCapture.setOnClickListener{
            val intent = Intent(leftCapture.context, CaptureActivity::class.java)
            leftCapture.context.startActivity(intent)
        }
    }
}