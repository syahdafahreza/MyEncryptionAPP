package com.myproject.myencryptionapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent

class MainMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val button = findViewById<Button>(R.id.btnEncryptActivity)
        val button2 = findViewById<Button>(R.id.btnDecryptActivity)
        button.setOnClickListener {
            val intent1 = Intent(this, EncryptActivity::class.java)
            startActivity(intent1)
        }
        button2.setOnClickListener{
            val intent2 = Intent(this, DecryptActivity::class.java)
            startActivity(intent2)
        }
    }
}