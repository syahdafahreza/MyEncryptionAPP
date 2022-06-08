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
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
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

class EncryptActivityIMG : AppCompatActivity() {

//    lateinit var myDir: File
    lateinit var myDirStcUri: File

    companion object {
        private val FILE_NAME_ENC = "image_enc"
        private val FILE_NAME_DEC = "image_dec.jpg"
        private val key = "PDY80oOtPHNYz1FG7"
        private val specString = "yoe6Nd84MOZCzbbO"
        var inputImgURI = ""
        var initialpickernameIMG: String = ""
    }

    val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
    val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX
    val cur_buffer_size=1024

    @RequiresApi(Build.VERSION_CODES.R)
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encrypt_img)

        Dexter.withActivity(this)
            .withPermissions(*arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .withListener(object:MultiplePermissionsListener{
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    Toast.makeText(this@EncryptActivityIMG,"You should accept permission",Toast.LENGTH_SHORT).show()
                }


            })
            .check()

        val root = Environment.getExternalStorageDirectory().toString()
        val buttonOpenIMG2Encrypt = findViewById<Button>(R.id.btnOpenImg2Encrypt)
        val buttonBeginEncryptIMG = findViewById<Button>(R.id.btnBeginEncryptIMG)

        buttonOpenIMG2Encrypt.setOnClickListener {
            Log.d("Info","Button Open File Clicked")
            openIMGFileUnencrypted("$root".toUri())
        }

        buttonBeginEncryptIMG.setOnClickListener {
            Log.d("Info","Button Encrypt IMG Clicked")

            saveIMGFileEncrypted("$root".toUri())

        }

    }
    @Suppress("DEPRECATION")
    fun openIMGFileUnencrypted(pickerInitialUri: Uri) {
        val intentOpenUnencrypted = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentOpenUnencrypted, 21)
    }
    @Suppress("DEPRECATION")
    fun saveIMGFileEncrypted(pickerInitialUri: Uri) {
        val intentSaveIMGEncrypted = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(Intent.EXTRA_TITLE, initialpickernameIMG+"_enc")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentSaveIMGEncrypted, 22)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 21
            && resultCode == Activity.RESULT_OK
        ) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri21 ->
                // Perform operations on the document using its URI.
                inputImgURI = uri21.toString()
                val buttonEncryptState = findViewById<Button>(R.id.btnBeginEncryptIMG)
                buttonEncryptState.setEnabled(true)
                dumpImageMetaData(uri21)
                readImageFromUri(uri21)
            }
        }
        else if (requestCode == 22
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri22 ->

                try {
                    encryptIMGToFile(
                        key,
                        specString,
                        inputImgURI.toUri(),
                        uri22
                    )

                    Toast.makeText(this, "Encrypted!", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }
    @Throws(IOException::class)
    fun readImageFromUri(uri21: Uri) {

        val imageUri: Uri = uri21
        val my_img_view = findViewById<ImageView>(R.id.imageView2Encrypt)
        my_img_view.setImageURI(imageUri)
    }

    @Throws(NoSuchAlgorithmException::class,
    NoSuchPaddingException::class,
    InvalidKeyException::class,
    IOException::class,
    InvalidAlgorithmParameterException::class)
    fun encryptIMGToFile(keyStr: String, spec: String,uri21: Uri, uri22: Uri) {

        var input: InputStream? = contentResolver.openInputStream(uri21)
        var output: OutputStream? = contentResolver.openOutputStream(uri22)
        try {
            val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec =
                PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

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
                val str1 = displayName
                val n = 4
                val resultsbstr = str1.dropLast(n)
                initialpickernameIMG = resultsbstr
                Log.i("TAG", "Display Name: $resultsbstr")

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

}