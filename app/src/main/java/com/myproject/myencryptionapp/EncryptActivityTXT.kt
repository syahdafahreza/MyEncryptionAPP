package com.myproject.myencryptionapp

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_TITLE
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptActivityTXT : AppCompatActivity() {
    val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
    val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX

    var aes_encrypt_unencode: ByteArray? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt_txt)

        val buttonEncrypt = findViewById<Button>(R.id.btnBeginEncryptTXT)
        val buttonCopyEncryptedText = findViewById<Button>(R.id.btnCopyEncryptedText)
        val buttonOpenFileToEncrypt = findViewById<Button>(R.id.btnOpenTXTFileToEncrypt)
        val buttonSaveToFileEncrypt = findViewById<Button>(R.id.btnSaveToFileEncrypt)
        buttonEncrypt.setOnClickListener {
            val txt_to_enc = findViewById<EditText>(R.id.fieldTextToEncrypt).text.toString()

            if (txt_to_enc!="") {
                encrypt(txt_to_enc)
            }
            else {
                Log.d("Info","txt_to_enc is null!")
            }
        }
        buttonCopyEncryptedText.setOnClickListener {
            copyEncryptedTextPRC()
        }
        buttonOpenFileToEncrypt.setOnClickListener{
            Log.d("Info","Button Open File Clicked")
            openFileUnencrypted("")
        }

        buttonSaveToFileEncrypt.setOnClickListener {
            Log.d("Info","Button Save File Clicked")
            @Suppress("DEPRECATION")
            saveFileEncrypted()
        }

    }

    @Suppress("DEPRECATION")
    fun openFileUnencrypted(pickerInitialUri: String) {
        val intentOpenUnencrypted = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentOpenUnencrypted, 1)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 1
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri ->
                // Perform operations on the document using its URI.
                print(resultData)
                Log.d("Isi Result Data",uri.toString())
                readTextFromUri(uri)
                val contentResolver = applicationContext.contentResolver

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                // Check for the freshest data.
                contentResolver.takePersistableUriPermission(uri, takeFlags)

            }
        }
        else if (requestCode == 2
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri2 ->
                // Perform operations on the document using its URI.
//                val hasil_enc_aes_sv =
                val aes_enc_txt = findViewById<EditText>(R.id.fieldEncryptedText).text.toString()
                saveEncryptedText(aes_encrypt_unencode, applicationContext, uri2)

            }
        }
    }

    fun saveEncryptedText(text: ByteArray?, context: Context, uri2: Uri) {
        try {

            val contentResolver = applicationContext.contentResolver
            val output: OutputStream? = contentResolver.openOutputStream(uri2)
            if (output != null) {
                output.write(text)
            }
            if (output != null) {
                output.flush()
            }
            if (output != null) {
                output.close()
            }
            Toast.makeText(context, "Text saved successfuly!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {

        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        Log.d("String Builder: ", stringBuilder.toString())
        var readTxt = findViewById<EditText>(R.id.fieldTextToEncrypt)
        readTxt.setText(stringBuilder.toString())
        return stringBuilder.toString()
    }

    private fun copyEncryptedTextPRC() {
        var encTextToCopy = findViewById<EditText>(R.id.fieldEncryptedText)
        val textToCopy = encTextToCopy.text.toString()
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }
    @Suppress("DEPRECATION")
    fun saveFileEncrypted() {
        val intentSaveEncrypted = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(EXTRA_TITLE,"encrypted.txt")
        }
        startActivityForResult(intentSaveEncrypted, 2)
    }

    private fun encryptProcess() {
//        val fieldOriText = findViewById<EditText>(R.id.fieldTextToEncrypt)
//        val encryptextTextResult = findViewById<EditText>(R.id.fieldEncryptedText) as EditText
//        val oriString = fieldOriText.text.toString()
//        val encodedString: String = Base64.getEncoder().encodeToString(oriString.toByteArray())
//        println(encodedString)
//        encryptextTextResult.setText(encodedString)
    }

    fun encrypt(strToEncrypt: String): String? {
        try {
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =
                PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

            val hasil_enc_aes = Base64.encode(
                cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)),
                Base64.DEFAULT
            )
            val hasil_enc_aes_encodestring = Base64.encodeToString(
                cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)),
                Base64.DEFAULT
            )
            println("Hasil Enc Aes = " + hasil_enc_aes)
            Log.d("hsl_enc_aes_uncd", hasil_enc_aes.toString())
            aes_encrypt_unencode = hasil_enc_aes
            Log.d("aes_encrypt_unencode", aes_encrypt_unencode.toString())
            val aes_enc_txt = findViewById<TextView>(R.id.fieldEncryptedText)
            aes_enc_txt.setText(hasil_enc_aes_encodestring)

            return hasil_enc_aes_encodestring

        } catch (e: Exception) {
            println("Error while encrypting: $e")
            val aes_enc_txt = findViewById<TextView>(R.id.fieldEncryptedText)
            aes_enc_txt.setText("Error while encrypting")
        }
        return null
    }


//    fun decrypt(strToDecrypt: String): String? {
//        try {
//
//            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
//
//            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
//            val spec =
//                PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
//            val tmp = factory.generateSecret(spec);
//            val secretKey = SecretKeySpec(tmp.encoded, "AES")
//
//            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
//
//            val hasil_dec = String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
//            println("Hasil Decript = " + hasil_dec)
//
//            val tv_dec = findViewById<TextView>(R.id.tv_hasil_dekripsi)
//            tv_dec.setText(hasil_dec)
//
//
//            return hasil_dec //String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
//
//
//        } catch (e: Exception) {
//            println("Error while decrypting: $e");
//            val tv_dec = findViewById<TextView>(R.id.tv_hasil_dekripsi)
//            tv_dec.setText("Error while decrypting")
//        }
//        return null
//    }
}