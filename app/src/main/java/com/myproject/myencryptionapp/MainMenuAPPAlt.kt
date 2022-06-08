package com.myproject.myencryptionapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainMenuAPPAlt : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu_app_alt)

        val buttonshowtxt_alt = findViewById<Button>(R.id.btnShowTextMenuAlt)
        val buttonshowimg_alt = findViewById<Button>(R.id.btnShowImageMenuAlt)
        val buttonshowpdf_alt = findViewById<Button>(R.id.btnShowPDFMenuAlt)

        buttonshowtxt_alt.setOnClickListener {
            val intent_txtmenu_alt = Intent(this, MainMenuTXT::class.java)
            startActivity(intent_txtmenu_alt)
        }
        buttonshowimg_alt.setOnClickListener {
            val intent_imgmenu_alt = Intent (this, MainMenuIMG::class.java)
            startActivity(intent_imgmenu_alt)
        }
        buttonshowpdf_alt.setOnClickListener {
            val intent_pdfmenu_alt = Intent(this, MainMenuPDF::class.java)
            startActivity(intent_pdfmenu_alt)
        }
    }
}