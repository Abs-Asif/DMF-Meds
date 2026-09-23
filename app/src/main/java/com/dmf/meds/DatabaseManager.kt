package com.dmf.meds

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DatabaseManager {
    private const val PREFS_NAME = "dmf_meds_prefs"
    private const val KEY_LAST_UPDATED_DATE = "last_updated_date"
    private const val KEY_LAST_SATURDAY_PROMPTED = "last_saturday_prompted"
    private const val DOWNLOAD_URL = "https://raw.githubusercontent.com/29MayStudio/MedEx/refs/heads/main/list.json"

    fun getLastUpdatedDate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_UPDATED_DATE, null) ?: "Default Database"
    }

    fun setLastUpdatedDate(context: Context, dateStr: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_UPDATED_DATE, dateStr).apply()
    }

    fun shouldPromptSaturdayUpdate(context: Context): Boolean {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            return false
        }
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastPrompted = prefs.getString(KEY_LAST_SATURDAY_PROMPTED, "")
        return lastPrompted != dateStr
    }

    fun markSaturdayPrompted(context: Context) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_SATURDAY_PROMPTED, dateStr).apply()
    }

    suspend fun loadMedicines(context: Context): List<Medicine> = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "list.json")
        val gson = Gson()
        if (file.exists() && file.length() > 0) {
            try {
                file.inputStream().use { stream ->
                    InputStreamReader(stream).use { reader ->
                        val remoteType = object : TypeToken<List<RemoteMedicine>>() {}.type
                        val remoteList: List<RemoteMedicine> = gson.fromJson(reader, remoteType)
                        if (!remoteList.isNullOrEmpty()) {
                            return@withContext remoteList.map { it.toMedicine() }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to asset medicine_data.json if local list.json does not exist
        context.assets.open("medicine_data.json").use { stream ->
            InputStreamReader(stream).use { reader ->
                val medicType = object : TypeToken<List<Medicine>>() {}.type
                return@withContext gson.fromJson(reader, medicType)
            }
        }
    }

    suspend fun downloadLatestDatabase(
        context: Context,
        onProgress: (Float) -> Unit
    ): Result<List<Medicine>> = withContext(Dispatchers.IO) {
        val tempFile = File(context.filesDir, "list.json.tmp")
        val finalFile = File(context.filesDir, "list.json")

        try {
            val url = URL(DOWNLOAD_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("HTTP Error: ${connection.responseCode}"))
            }

            val totalLength = connection.contentLength.toLong()
            var downloaded = 0L

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (totalLength > 0) {
                            val progress = downloaded.toFloat() / totalLength.toFloat()
                            onProgress(progress)
                        }
                    }
                }
            }

            // Parse downloaded JSON to verify valid format
            val gson = Gson()
            val remoteList: List<RemoteMedicine> = tempFile.inputStream().use { stream ->
                InputStreamReader(stream).use { reader ->
                    val remoteType = object : TypeToken<List<RemoteMedicine>>() {}.type
                    gson.fromJson(reader, remoteType)
                }
            }

            if (remoteList.isNullOrEmpty()) {
                tempFile.delete()
                return@withContext Result.failure(Exception("Downloaded database is empty or invalid"))
            }

            // Replace existing list.json with tempFile
            if (finalFile.exists()) {
                finalFile.delete()
            }
            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            val todayStr = SimpleDateFormat("d MMMM yyyy", Locale.US).format(Date())
            setLastUpdatedDate(context, todayStr)
            markSaturdayPrompted(context)

            val medicines = remoteList.map { it.toMedicine() }
            Result.success(medicines)
        } catch (e: Exception) {
            if (tempFile.exists()) tempFile.delete()
            Result.failure(e)
        }
    }
}
