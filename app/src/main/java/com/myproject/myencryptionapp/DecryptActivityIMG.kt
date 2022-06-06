package com.myproject.myencryptionapp

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.myproject.myencryptionapp.utils.MyEncryptionMakerIMG
import java.io.*
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DecryptActivityIMG : AppCompatActivity() {

    lateinit var myDirStc: File

    companion object {
        private val FILE_NAME_ENC = "image_enc"
        private val FILE_NAME_DEC = "image_dec.jpg"
        private val key = "PDY80oOtPHNYz1FG7"
        private val specString = "yoe6Nd84MOZCzbbO"
    }
    val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
    val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX
    val cur_buffer_size=1024
    var isiuri21 = ""
    var isiuri22 = ""

    @RequiresApi(Build.VERSION_CODES.R)
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt_img)

        Dexter.withActivity(this)
            .withPermissions(*arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    Toast.makeText(this@DecryptActivityIMG,"You should accept permission", Toast.LENGTH_SHORT).show()
                }


            })
            .check()
        val root = Environment.getExternalStorageDirectory().toString()
        val rootnonstring = Environment.getExternalStorageDirectory()
        val rootnonstringAlt = Environment.getStorageDirectory()
        myDirStc = File("$root/MyEncryptionAPP")
        if (!myDirStc.exists()) {
            myDirStc.mkdirs()
        }
        Log.d("root",root)
        Log.d("rootnotstring",rootnonstring.toString())
        Log.d("rootnonstringAlt",rootnonstringAlt.toString())

        val buttonBeginDecryptIMAGE = findViewById<Button>(R.id.btnBeginDecryptIMG)

        buttonBeginDecryptIMAGE.setOnClickListener {
            val outputFileDec = File(myDirStc, FILE_NAME_DEC)
            val encFile = File(myDirStc, FILE_NAME_ENC)

            try {
                MyEncryptionMakerIMG.decryptToFile(
                    key,
                    specString,
                    FileInputStream(encFile),
                    FileOutputStream(outputFileDec)
                )
                val imageViewDecryptedURI = findViewById<ImageView>(R.id.imageViewDecrypted)
                imageViewDecryptedURI.setImageURI(Uri.fromFile(outputFileDec))
//                outputFileDec.delete()

                Toast.makeText(this, "Decrypted!", Toast.LENGTH_SHORT).show()
                // Handler which will run after 2 seconds.
                // Handler which will run after 2 seconds.
                Handler().postDelayed(Runnable {
                    Toast.makeText(
                        this,
                        "Your decrypted image stored in: \n"+outputFileDec.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }, 2000)
//                Toast.makeText(this, "Your decrypted image stored in:"+outputFileDec.toString(), Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class)
    fun decryptIMGToFile(input: InputStream, output: OutputStream) {
//        val contentResolver = applicationContext.contentResolver
        var output=output
//        var output: OutputStream? = contentResolver.openOutputStream(uri22)
        try {
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =
                PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

            output = CipherOutputStream(output,cipher)
            val buffer = ByteArray(cur_buffer_size)
            Log.d("COutput", output.toString())
            var bytesRead:Int = 0
            while(input.read(buffer).also { bytesRead = it } > 0)
                output.write(buffer, 0, bytesRead)
        } finally {
            Log.d("Output buffer",output.toString())
            Log.d("Output","output.flush()")
            if (output != null) {
                output.flush()
            }
            Log.d("Output","output.close()")
            if (output != null) {
                output.close()
            }
        }

    }
}