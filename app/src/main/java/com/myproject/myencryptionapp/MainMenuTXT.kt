package com.myproject.myencryptionapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent

class MainMenuTXT : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_txt)

        val button = findViewById<Button>(R.id.btnEncryptTXTActivity)
        val button2 = findViewById<Button>(R.id.btnDecryptTXTActivity)
        button.setOnClickListener {
            val intent1 = Intent(this, EncryptActivityTXT::class.java)
            startActivity(intent1)
        }
        button2.setOnClickListener{
            val intent2 = Intent(this, DecryptActivityTXT::class.java)
            startActivity(intent2)
        }
    }
}