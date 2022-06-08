package com.myproject.myencryptionapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.myproject.myencryptionapp.EncryptActivityPDF.HumanizeUtils.formatAsFileSize
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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
import kotlin.math.log2
import kotlin.math.pow

class DecryptActivityPDF : AppCompatActivity() {

    companion object {
        private val FILE_NAME_ENC = "image_enc"
        private val FILE_NAME_DEC = "image_dec.jpg"
        private val key = "PDY80oOtPHNYz1FG7"
        private val specString = "yoe6Nd84MOZCzbbO"
        private var inputPDFUri = ""
        private var outputPDFUri = ""
        private var initialpickernamePDFIn: String = ""
        private var initialpickernamePDFOut: String = ""
        private var initialsize: String = ""
        private var initialsizeformat: Int = 0
    }
    object HumanizeUtils {
        val File.formatSize: String
            get() = length().formatAsFileSize

        val Int.formatAsFileSize: String
            get() = toLong().formatAsFileSize

        val Long.formatAsFileSize: String
            get() = log2(if (this != 0L) toDouble() else 1.0).toInt().div(10).let {
                val precision = when (it) {
                    0 -> 0; 1 -> 1; else -> 2
                }
                val prefix = arrayOf("", "K", "M", "G", "T", "P", "E", "Z", "Y")
                String.format("%.${precision}f ${prefix[it]}B", toDouble() / 2.0.pow(it * 10.0))
            }
    }

    val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    val salt = "QWlGNHNhMTJTQWZ2bGhpV3U=" // base64 decode => AiF4sa12SAfvlhiWu
    val iv = "bVQzNFNhRkQ1Njc4UUFaWA==" // base64 decode => mT34SaFD5678QAZX
    val cur_buffer_size=1024

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decrypt_pdf)

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
                    Toast.makeText(this@DecryptActivityPDF,"You should accept permission", Toast.LENGTH_SHORT).show()
                }


            })
            .check()

        val root = Environment.getExternalStorageDirectory().toString()

        val buttonOpenFilePDFEnc = findViewById<Button>(R.id.btnOpenFilePDFEncrypted)
        val buttonSaveFilePDFDec = findViewById<Button>(R.id.btnBeginDecryptPDF)

        buttonOpenFilePDFEnc.setOnClickListener {
            openPDFFileEnc("$root".toUri())
        }
        buttonSaveFilePDFDec.setOnClickListener {
            savePDFFileDec("$root".toUri())
        }
    }
    // Request code for selecting a PDF document.
    @Suppress("DEPRECATION")
    fun openPDFFileEnc(pickerInitialUri: Uri) {
        val intentOpenPDFFileEnc = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentOpenPDFFileEnc, 33)
    }
    // Request code for creating a PDF document.
    @Suppress("DEPRECATION")
    private fun savePDFFileDec(pickerInitialUri: Uri) {
        val intentSavePDFFileDec = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, initialpickernamePDFIn+".pdf")

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentSavePDFFileDec, 34)
    }
    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 33
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri33 ->
                // Perform operations on the document using its URI.
                dumpImageMetaData(uri33)
                inputPDFUri = uri33.toString()
                val pdficonstate = findViewById<ImageView>(R.id.imageViewPDFIconStateDec)
                val buttonDecryptSavePDFstate = findViewById<Button>(R.id.btnBeginDecryptPDF)
                var filename = findViewById<TextView>(R.id.filenamePDFTextViewDec)
                val size = findViewById<TextView>(R.id.filesizePDFTextViewDec)
                buttonDecryptSavePDFstate.setEnabled(true)
                filename.setText(initialpickernamePDFIn)
                val sizeformat = initialsizeformat.formatAsFileSize
                size.setText(sizeformat)
                println("968542985:  "+13.formatAsFileSize)
                pdficonstate.setImageDrawable(getResources().getDrawable(R.drawable.pdfinputfile_enc));
            }
        }
        else if (requestCode == 34
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri34 ->
                // Perform operations on the document using its URI.
                try {
                    decryptPDFToFile(
                        key,
                        specString,
                        inputPDFUri.toUri(),
                        uri34
                    )

                    Toast.makeText(this, "Decrypted!", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        IOException::class,
        InvalidAlgorithmParameterException::class)
    fun decryptPDFToFile(keyStr: String, spec: String,uri33: Uri, uri34: Uri) {

        var input: InputStream? = contentResolver.openInputStream(uri33)
        var output: OutputStream? = contentResolver.openOutputStream(uri34)
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
                initialpickernamePDFIn = resultsbstr
                Log.i("TAG", "Display Name Disunat: $resultsbstr")

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

                initialsize = size
                initialsizeformat = size.toInt()
            }
        }
    }
}