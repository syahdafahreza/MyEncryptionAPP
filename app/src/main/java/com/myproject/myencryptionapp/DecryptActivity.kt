package com.myproject.myencryptionapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import java.util.*

class DecryptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt)

        val buttonDecrypt = findViewById<Button>(R.id.btnBeginDecrypt)
        val buttonCopyDecryptedText = findViewById<Button>(R.id.btnCopyDecryptedText)

        buttonDecrypt.setOnClickListener {
            decryptProcess()
        }
        buttonCopyDecryptedText.setOnClickListener{
            copyDecryptedTextPRC()
        }
    }

    private fun copyDecryptedTextPRC() {

    }

    private fun decryptProcess() {
        val encodedString = findViewById<EditText>(R.id.fieldEncryptedTextToDecrypt) as EditText
        val decryptedTextResult = findViewById<EditText>(R.id.fieldDecryptedText) as EditText
        val decodedBytes = Base64.getDecoder().decode(encodedString.text.toString())
        val decodedString = String(decodedBytes)
        println(decodedString)
        decryptedTextResult.setText(decodedString)
    }

}