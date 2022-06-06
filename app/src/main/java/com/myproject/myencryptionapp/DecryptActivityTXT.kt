package com.myproject.myencryptionapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.io.*
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DecryptActivityTXT : AppCompatActivity() {
    val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
    val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt_txt)

        val buttonOpenEncrypted = findViewById<Button>(R.id.btnOpenFileToDecrypt)
        val buttonDecrypt = findViewById<Button>(R.id.btnBeginDecryptTXT)
        val buttonCopyDecryptedText = findViewById<Button>(R.id.btnCopyDecryptedText)
        val buttonSaveDecryptedText = findViewById<Button>(R.id.btnSaveToFileDecrypt)

        buttonDecrypt.setOnClickListener {
            val encodedString = findViewById<EditText>(R.id.fieldEncryptedTextToDecrypt).text.toString()
            if (encodedString!=""){
                decrypt(encodedString)
            }
            else {
                Log.d("Info","encodedString is null!")
            }
        }
        buttonCopyDecryptedText.setOnClickListener{
            copyDecryptedTextPRC()
        }
        buttonOpenEncrypted.setOnClickListener {
            openFileEncrypted("")
        }
        buttonSaveDecryptedText.setOnClickListener {
            Log.d("Info","Button Save File Clicked")
            @Suppress("DEPRECATION")
            saveFileDecrypted()
        }
    }

    @Suppress("DEPRECATION")
    fun openFileEncrypted(pickerInitialUri: String) {
        val intentOpenUnencrypted = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentOpenUnencrypted, 3)
    }

    @Suppress("DEPRECATION")
    fun saveFileDecrypted() {
        val intentSaveEncrypted = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(Intent.EXTRA_TITLE,"decrypted.txt")
        }
        startActivityForResult(intentSaveEncrypted, 4)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 3
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri3 ->
                // Perform operations on the document using its URI.
                print(resultData)
                Log.d("Isi Result Data",uri3.toString())
                readTextFromUri(uri3)
                val contentResolver = applicationContext.contentResolver

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                // Check for the freshest data.
                contentResolver.takePersistableUriPermission(uri3, takeFlags)

            }
        }
        else if (requestCode == 4
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri4 ->
                // Perform operations on the document using its URI.
                val tf_dec_txt = findViewById<EditText>(R.id.fieldDecryptedText).text.toString()
                saveDecryptedText(tf_dec_txt.toByteArray(), applicationContext, uri4)
            }
        }
    }

    fun saveDecryptedText(text: ByteArray, context: Context, uri4: Uri) {
        try {

            val contentResolver = applicationContext.contentResolver
            val output: OutputStream? = contentResolver.openOutputStream(uri4)
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
    private fun readTextFromUri(uri3: Uri,): String {

        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri3)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }

        Log.d("String Builder: ", stringBuilder.toString())
        var readEncTxt = findViewById<EditText>(R.id.fieldEncryptedTextToDecrypt)


        readEncTxt.setText(stringBuilder.toString())
//        val sb2 = stringBuilder.toString()
//        base64Decode(sb2)
        return stringBuilder.toString()
    }

    fun base64Decode(strEmboh: String): String? {
        val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec =
            PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
        val tmp = factory.generateSecret(spec)
        val secretKey = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val stringBuilder2 = Base64.encodeToString(
            cipher.doFinal(strEmboh.toByteArray(Charsets.UTF_8)),
            Base64.DEFAULT
        )
        var readEncTxt2 = findViewById<EditText>(R.id.fieldEncryptedTextToDecrypt)
        readEncTxt2.setText(stringBuilder2.toString())
        return strEmboh
    }

    private fun copyDecryptedTextPRC() {

    }

//    private fun decryptProcess() {
//        val encodedString = findViewById<EditText>(R.id.fieldEncryptedTextToDecrypt) as EditText
//        val decryptedTextResult = findViewById<EditText>(R.id.fieldDecryptedText) as EditText
//        val decodedBytes = Base64.getDecoder().decode(encodedString.text.toString())
//        val decodedString = String(decodedBytes)
//        println(decodedString)
//        decryptedTextResult.setText(decodedString)
//    }

    fun decrypt(strToDecrypt: String): String? {
        try {

            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =
                PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec);
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            val hasil_dec_aes = String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))
            println("Hasil Decript = " + hasil_dec_aes)

            val tv_dec_aes = findViewById<TextView>(R.id.fieldDecryptedText)
            tv_dec_aes.setText(hasil_dec_aes)


            return hasil_dec_aes //String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT)))


        } catch (e: Exception) {
            println("Error while decrypting: $e");
            val tv_dec_aes = findViewById<TextView>(R.id.fieldDecryptedText)
            tv_dec_aes.setText("Error while decrypting")
        }
        return null
    }
}