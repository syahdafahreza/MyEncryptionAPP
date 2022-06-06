package com.myproject.myencryptionapp

import android.app.Activity
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_TITLE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.drawToBitmap
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
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

class EncryptActivityIMG : AppCompatActivity() {

//    lateinit var myDir: File
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
        val rootnonstring = Environment.getExternalStorageDirectory()
        val rootnonstringAlt = Environment.getStorageDirectory()
        myDirStc = File("$root/MyEncryptionAPP")
        if (!myDirStc.exists()) {
            myDirStc.mkdirs()
        }
        Log.d("root",root)
        Log.d("rootnotstring",rootnonstring.toString())
        Log.d("rootnonstringAlt",rootnonstringAlt.toString())
        val buttonOpenIMG2Encrypt = findViewById<Button>(R.id.btnOpenImg2Encrypt)
        val buttonBeginEncryptIMG = findViewById<Button>(R.id.btnBeginEncryptIMG)

        buttonOpenIMG2Encrypt.setOnClickListener {
            Log.d("Info","Button Open File Clicked")
            openIMGFileUnencrypted(rootnonstring.toUri())
        }

        buttonBeginEncryptIMG.setOnClickListener {
            Log.d("Info","Button Encrypt IMG Clicked")
            val my_img_view = findViewById<ImageView>(R.id.imageView2Encrypt)
            val drawable = my_img_view.getDrawable()
            val bitmapDrawable = drawable as BitmapDrawable
            val bitmap = bitmapDrawable.bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val input = ByteArrayInputStream(stream.toByteArray())
            val contentResolver = applicationContext.contentResolver
            val outputFileEnc = File(myDirStc, FILE_NAME_ENC)
            try {
                MyEncryptionMakerIMG.encryptToFile(
                    key,
                    specString,
                    input,
                    FileOutputStream(outputFileEnc)
                )

                Toast.makeText(this, "Encrypted.", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
            }
//            saveIMGFileEncrypted(rootnonstring.toUri())

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
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 21
            && resultCode == Activity.RESULT_OK
        ) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri21 ->
                // Perform operations on the document using its URI.
                print(resultData)
                Log.d("uri21.toString()", uri21.toString())
                readImageFromUri(uri21)
                val contentResolver = applicationContext.contentResolver

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                // Check for the freshest data.
                contentResolver.takePersistableUriPermission(uri21, takeFlags)

            }
        }
        else if (requestCode == 22
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri22 ->
                // Perform operations on the document using its URI.
//                val imageUri: Uri = uri
                isiuri22 = uri22.toString()
                Log.d("uri22tostr",uri22.toString())
//                val my_img_view = findViewById<ImageView>(R.id.imageView2Encrypt)
//                val outputFileEnc = my_img_view.getDrawable()
//                val drawable = outputFileEnc
//                val bitmapDrawable = drawable as BitmapDrawable
//                val bitmap = bitmapDrawable.bitmap
//                val stream = ByteArrayOutputStream()
//                myDir = File(uri22.toString())
//
//                val input = ByteArrayInputStream(stream.toByteArray())
//                bitmap.compress(Bitmap.CompressFormat.JPEG,100, stream)
//                val contentResolver = applicationContext.contentResolver
//
//                try {
//                    var myDir2Out: OutputStream? = contentResolver.openOutputStream(uri22)
//                    Log.d("myDir",myDir.toString())
//                    Log.d("isiuri22",isiuri22)
//                    encryptIMGToFile(input, FileOutputStream(), applicationContext, uri22)
//
//
//                } catch (e:Exception) {
//                    e.printStackTrace()
//                }

            }
        }
    }
    @Throws(IOException::class)
    fun readImageFromUri(uri21: Uri) {

        val imageUri: Uri = uri21
        isiuri21 = imageUri.toString()
        val my_img_view = findViewById<ImageView>(R.id.imageView2Encrypt)
        my_img_view.setImageURI(imageUri)
    }

    @Throws(NoSuchAlgorithmException::class,
    NoSuchPaddingException::class,
    InvalidKeyException::class,
    IOException::class,
    InvalidAlgorithmParameterException::class)
    fun encryptIMGToFile(input: InputStream,output: OutputStream) {
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
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

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

    @Suppress("DEPRECATION")
    fun saveIMGFileEncrypted(pickerInitialUri: Uri) {
        val intentSaveIMGEncrypted = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(Intent.EXTRA_TITLE,"your_encrypted_image_enc")
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentSaveIMGEncrypted, 22)
    }
}