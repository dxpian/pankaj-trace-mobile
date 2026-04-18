package myandroid.app.hhobzic.inventorymobile.utility

import android.content.Context
import android.os.Environment
import android.util.Log
import myandroid.app.hhobzic.inventorymobile.BuildConfig
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {
    private const val LOG_FILE_NAME = "inventory.txt"
    private var logFile: File? = null
    private const val TAG = "APP Logger"
    fun init(context: Context) {
        if (BuildConfig.DEBUG)
        {
            val logDir = File(context.getExternalFilesDir(null), "logs")
            if (!logDir.exists()) logDir.mkdirs()
            logFile = File(logDir, LOG_FILE_NAME)
         }
    }

    private fun writeToFile(tag: String, level: String, message: String) {
        if(logFile == null) {
            Log.e(this.TAG, "Log handler is invalid. Cannot write log data")
            return
        }
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val logLine = "$timestamp [$level] [$tag]: $message\n"
            FileWriter(logFile!!, true).use { it.write(logLine) }
        } catch (e: IOException) {
            Log.e("AppLogger", "Failed to write log: ${e.message}")
        }
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        writeToFile(tag, "DEBUG", message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        writeToFile(tag, "INFO", message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        writeToFile(tag, "WARN", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
        writeToFile(tag, "ERROR", "$message ${throwable?.stackTraceToString() ?: ""}")
    }
}
