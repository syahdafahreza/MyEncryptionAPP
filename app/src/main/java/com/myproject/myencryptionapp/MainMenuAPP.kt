package com.myproject.myencryptionapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainMenuAPP : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_app)

        val buttonshowtxt = findViewById<Button>(R.id.btnShowTextMenu)
        val buttonshowimg = findViewById<Button>(R.id.btnShowImageMenu)
        val buttonshowpdf = findViewById<Button>(R.id.btnShowPDFMenu)

        buttonshowtxt.setOnClickListener {
            val intent_txtmenu = Intent(this, MainMenuTXT::class.java)
            startActivity(intent_txtmenu)
        }
        buttonshowimg.setOnClickListener {
            val intent_imgmenu = Intent (this, MainMenuIMG::class.java)
            startActivity(intent_imgmenu)
        }
        buttonshowpdf.setOnClickListener {
            val intent_pdfmenu = Intent(this, MainMenuPDF::class.java)
            startActivity(intent_pdfmenu)
        }
    }
}