package moji.deliverytracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Driver::class,
        Customer::class,
        Neighborhood::class,
        Order::class,
        Payment::class,
        CommissionHistory::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun driverDao(): DriverDao
    abstract fun customerDao(): CustomerDao
    abstract fun neighborhoodDao(): NeighborhoodDao
    abstract fun orderDao(): OrderDao
    abstract fun paymentDao(): PaymentDao
    abstract fun commissionHistoryDao(): CommissionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "DeliveryDB"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
