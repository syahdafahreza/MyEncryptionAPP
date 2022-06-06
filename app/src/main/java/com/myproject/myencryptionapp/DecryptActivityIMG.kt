package com.myproject.myencryptionapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.myproject.myencryptionapp.utils.MyEncryptionMakerIMG
import java.io.*
import java.net.URI
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

    companion object {
        private val FILE_NAME_ENC = "image_enc"
        private val FILE_NAME_DEC = "image_dec.jpg"
        private val key = "PDY80oOtPHNYz1FG7"
        private val specString = "yoe6Nd84MOZCzbbO"
        var inputan = ""
        var initialpickername2: String = ""
    }
    val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
    val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX
    val cur_buffer_size=1024

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

        val buttonOpenImageFileEnc = findViewById<Button>(R.id.btnOpenIMGFileEncrypted)
        val buttonBeginDecryptIMAGE = findViewById<Button>(R.id.btnBeginDecryptIMG)

        buttonOpenImageFileEnc.setOnClickListener {
            openIMGFileEnc("$root".toUri())
        }
        buttonBeginDecryptIMAGE.setOnClickListener {
            saveIMGFileDec("$root".toUri())
        }
    }

    @Suppress("DEPRECATION")
    fun openIMGFileEnc(pickerInitialUri: Uri) {
        val intentOpenIMGFileEnc = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intentOpenIMGFileEnc, 23)
    }
    @Suppress("DEPRECATION")
    fun saveIMGFileDec(pickerInitialUri: Uri) {
        val intentSaveIMGFileDec = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(Intent.EXTRA_TITLE, initialpickername2+".jpg")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentSaveIMGFileDec, 24)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 23
            && resultCode == Activity.RESULT_OK
        ) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri23 ->
                // Perform operations on the document using its URI.
                val buttonDecryptState = findViewById<Button>(R.id.btnBeginDecryptIMG)
                buttonDecryptState.setEnabled(true)
                dumpImageMetaData(uri23)
                inputan = uri23.toString()
                Log.d("inputan", inputan)
            }
        }
        else if (requestCode == 24
                && resultCode == Activity.RESULT_OK
            ) {
                // The result data contains a URI for the document or directory that
                // the user selected.
                resultData?.data?.also { uri24 ->
                    // Perform operations on the document using its URI.

                    try {
                        decryptIMGToFile(
                            key,
                            specString,
                            inputan.toUri(),
                            uri24,
                        )
                        val imageUri2: Uri = uri24
                        val decryptedImageView = findViewById<ImageView>(R.id.imageViewDecrypted)
                        decryptedImageView.setImageURI(imageUri2)
//                outputFileDec.delete()

                        Toast.makeText(this, "Decrypted.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

    @SuppressLint("Range")
    fun dumpImageMetaData(uri: Uri) {

        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.
        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null)

        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (it.moveToFirst()) {

                // Note it's called "Display Name". This is
                // provider-specific, and might not necessarily be the file name.
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.i("TAG", "Display Name: $displayName")
                val str2 = displayName
                val n2 = 4
                val resultsbstr2 = str2.dropLast(n2)
                initialpickername2 = resultsbstr2
                Log.i("TAG", "Display Name Disunat: $resultsbstr2")

                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                // If the size is unknown, the value stored is null. But because an
                // int can't be null, the behavior is implementation-specific,
                // and unpredictable. So as
                // a rule, check if it's null before assigning to an int. This will
                // happen often: The storage API allows for remote files, whose
                // size might not be locally known.
                val size: String = if (!it.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.i("TAG", "Size: $size")
            }
        }
    }

    @Throws(NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class)
    fun decryptIMGToFile(keyStr: String, spec: String,uri23: Uri, uri24: Uri) {

        var input: InputStream? = contentResolver.openInputStream(uri23)
        var output: OutputStream? = contentResolver.openOutputStream(uri24)

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
            if (input != null) {
                while(input.read(buffer).also { bytesRead = it } > 0)
                    output.write(buffer, 0, bytesRead)
            }
        } finally {
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