package com.myproject.myencryptionapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainMenuPDF : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_pdf)

        val buttonEncryptPDFActivity = findViewById<Button>(R.id.btnEncryptPDFActivity)

        buttonEncryptPDFActivity.setOnClickListener {
            val intent_encryptpdfactvt = Intent(this,EncryptActivityPDF::class.java)
            startActivity(intent_encryptpdfactvt)
        }
    }
}