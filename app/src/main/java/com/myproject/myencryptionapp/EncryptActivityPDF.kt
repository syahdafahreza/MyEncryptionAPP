package com.myproject.myencryptionapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.PointF.length
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
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
import kotlin.math.log2
import kotlin.math.pow

class EncryptActivityPDF : AppCompatActivity() {

    companion object {
        private val FILE_NAME_ENC = "image_enc"
        private val FILE_NAME_DEC = "image_dec.jpg"
        private val key = "PDY80oOtPHNYz1FG7"
        private val specString = "yoe6Nd84MOZCzbbO"
        var inputan2Enc = ""
        var initialpickername: String = ""
        var initialpickername2: String = ""
        var initialsize: String = ""
        var initialsizeformat: Int = 0
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
        setContentView(R.layout.activity_encrypt_pdf)

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
                    Toast.makeText(this@EncryptActivityPDF,"You should accept permission", Toast.LENGTH_SHORT).show()
                }


            })
            .check()

        val root = Environment.getExternalStorageDirectory().toString()

        val buttonOpenFilePDFDec = findViewById<Button>(R.id.btnOpenFilePDF)


        buttonOpenFilePDFDec.setOnClickListener {
            openPDFFileDec("$root".toUri())
        }
    }
    // Request code for selecting a PDF document.
    @Suppress("DEPRECATION")
    fun openPDFFileDec(pickerInitialUri: Uri) {
        val intentOpenPDFFileDec = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentOpenPDFFileDec, 31)
    }
    // Request code for creating a PDF document.
    @Suppress("DEPRECATION")
    private fun savePDFFileEnc(pickerInitialUri: Uri) {
        val intentSavePDFFileEnc = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "invoice.pdf")

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intentSavePDFFileEnc, 32)
    }
    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == 31
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri31 ->
                // Perform operations on the document using its URI.
                dumpImageMetaData(uri31)
                val pdficonstate = findViewById<ImageView>(R.id.imageViewPDFIconState)
                val buttonEncryptSavePDFstate = findViewById<Button>(R.id.btnBeginEncryptPDF)
                var filename = findViewById<TextView>(R.id.filenamePDFTextView)
                val size = findViewById<TextView>(R.id.filesizePDFTextView)
                buttonEncryptSavePDFstate.setEnabled(true)
                filename.setText(initialpickername)
                val sizeformat = initialsizeformat.formatAsFileSize
                size.setText(sizeformat)
                println("968542985:  "+13.formatAsFileSize)
                pdficonstate.setImageDrawable(getResources().getDrawable(R.drawable.pdfinputfile));
            }
        }
        else if (requestCode == 32
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri32 ->
                // Perform operations on the document using its URI.

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
                initialpickername = resultsbstr
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