package com.myproject.myencryptionapp.utils

import java.io.File
import kotlin.math.log2
import kotlin.math.pow

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