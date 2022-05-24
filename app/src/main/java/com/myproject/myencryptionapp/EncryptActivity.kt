package com.myproject.myencryptionapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class EncryptActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt)

        val buttonEncrypt = findViewById<Button>(R.id.btnBeginEncrypt)
        val buttonCopyEncryptedText = findViewById<Button>(R.id.btnCopyEncryptedText)
        buttonEncrypt.setOnClickListener {
            encryptProcess()
        }
        buttonCopyEncryptedText.setOnClickListener{
            copyEncryptedTextPRC()
        }

    }

    private fun copyEncryptedTextPRC() {
        var encTextToCopy = findViewById<EditText>(R.id.fieldEncryptedText)
        val textToCopy = encTextToCopy.text.toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }

    private fun encryptProcess() {
        val fieldOriText = findViewById<EditText>(R.id.fieldTextToEncrypt)
        val encryptextTextResult = findViewById<EditText>(R.id.fieldEncryptedText) as EditText
        val oriString = fieldOriText.text.toString()
        val encodedString: String = Base64.getEncoder().encodeToString(oriString.toByteArray())
        println(encodedString)
        encryptextTextResult.setText(encodedString)
    }
}