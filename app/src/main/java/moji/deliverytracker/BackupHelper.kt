package moji.deliverytracker

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object BackupHelper {
    suspend fun exportToCSV(context: Context, db: AppDatabase): Pair<Boolean, String> {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MahanBackup")
                if (!dir.exists()) dir.mkdirs()

                val orders = db.orderDao().getAllWithNamesOnce()
                val file = File(dir, "orders_$timestamp.csv")
                FileWriter(file).use { writer ->
                    writer.append("ID,Customer,Driver,Neighborhood,Amount,Description,DateTime,Settled\n")
                    orders.forEach { order ->
                        writer.append("${order.id},${order.customerName},${order.driverName},${order.neighborhoodName},${order.amount},${order.description},${order.dateTime},${order.settled}\n")
                    }
                }
                Pair(true, file.absolutePath)
            } catch (e: Exception) {
                Pair(false, e.message ?: context.getString(R.string.backup_save_error))
            }
        }
    }
}
