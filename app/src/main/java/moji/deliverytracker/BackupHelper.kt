package moji.deliverytracker

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

object BackupHelper {

    /**
     * Check if storage permission is granted.
     * On Android 10+ with scoped storage, WRITE_EXTERNAL_STORAGE is not needed for Downloads.
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            true // Scoped storage handles Downloads directory via MediaStore
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    suspend fun exportToCSV(context: Context, db: AppDatabase): Pair<Boolean, String> {
        if (!hasStoragePermission(context)) {
            return Pair(false, context.getString(R.string.backup_permission_error))
        }

        return withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val orders = db.orderDao().getAllWithNamesOnce()
                val fileName = "orders_$timestamp.csv"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exportViaMediaStore(context, orders, fileName)
                } else {
                    exportViaLegacy(context, orders, fileName)
                }
            } catch (e: Exception) {
                Pair(false, e.message ?: context.getString(R.string.backup_save_error))
            }
        }
    }

    private fun exportViaMediaStore(
        context: Context,
        orders: List<OrderWithNames>,
        fileName: String
    ): Pair<Boolean, String> {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/MahanBackup")
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return Pair(false, context.getString(R.string.backup_save_error))

        resolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                writeCsvContent(writer, orders)
            }
        } ?: return Pair(false, context.getString(R.string.backup_save_error))

        return Pair(true, "Downloads/MahanBackup/$fileName")
    }

    @Suppress("DEPRECATION")
    private fun exportViaLegacy(
        context: Context,
        orders: List<OrderWithNames>,
        fileName: String
    ): Pair<Boolean, String> {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "MahanBackup"
        )
        if (!dir.exists()) dir.mkdirs()

        val file = File(dir, fileName)
        FileWriter(file).use { writer ->
            writeCsvContent(writer, orders)
        }
        return Pair(true, file.absolutePath)
    }

    private fun writeCsvContent(writer: java.io.Writer, orders: List<OrderWithNames>) {
        writer.append("ID,Customer,Driver,Neighborhood,Amount,Description,DateTime,Settled\n")
        orders.forEach { order ->
            writer.append(
                listOf(
                    order.id.toString(),
                    csvSafe(order.customerName),
                    csvSafe(order.driverName),
                    csvSafe(order.neighborhoodName),
                    order.amount.toString(),
                    csvSafe(order.description),
                    csvSafe(order.dateTime),
                    order.settled.toString()
                ).joinToString(",")
            )
            writer.append("\n")
        }
    }

    /**
     * Escape CSV value and prevent CSV injection.
     * Prefixes dangerous characters (=, +, -, @, |, %) with a single quote
     * to prevent formula execution in spreadsheet applications.
     */
    private fun csvSafe(value: String): String {
        var safe = value
        // Prevent CSV injection: prefix formula-triggering characters
        if (safe.isNotEmpty() && safe[0] in charArrayOf('=', '+', '-', '@', '|', '%')) {
            safe = "'$safe"
        }
        // Standard CSV escaping: double quotes and wrap
        val escaped = safe.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
