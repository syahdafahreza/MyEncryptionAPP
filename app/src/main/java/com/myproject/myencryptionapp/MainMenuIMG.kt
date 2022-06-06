package com.myproject.myencryptionapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainMenuIMG : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_img)

        val buttonEncryptIMGActvt = findViewById<Button>(R.id.btnEncryptIMGActivity)
        val buttonDecryptIMGActvt = findViewById<Button>(R.id.btnDecryptIMGActivity)

        buttonEncryptIMGActvt.setOnClickListener {
            val intent_encryptimgactivity = Intent (this, EncryptActivityIMG::class.java)
            startActivity(intent_encryptimgactivity)
        }
        buttonDecryptIMGActvt.setOnClickListener {
            val intent_decryptimgactivity = Intent (this, DecryptActivityIMG::class.java)
            startActivity(intent_decryptimgactivity)
        }
    }
}