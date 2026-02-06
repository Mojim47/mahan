package moji.deliverytracker

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Driver::class,
        Customer::class,
        Neighborhood::class,
        Order::class,
        Payment::class,
        CommissionHistory::class
    ],
    version = 7,
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

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("INSERT OR IGNORE INTO drivers(name, national_id, plate, phone, address, commission) VALUES('__UNKNOWN__','', '', '', '', 0)")
                db.execSQL("INSERT OR IGNORE INTO customers(name, national_id, phone, address) VALUES('__UNKNOWN__','', '', '')")
                db.execSQL("INSERT OR IGNORE INTO neighborhoods(name) VALUES('__UNKNOWN__')")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS orders_new(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id INTEGER NOT NULL,
                        driver_id INTEGER NOT NULL,
                        neighborhood_id INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        date_time TEXT NOT NULL,
                        settled INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        FOREIGN KEY(driver_id) REFERENCES drivers(id),
                        FOREIGN KEY(customer_id) REFERENCES customers(id),
                        FOREIGN KEY(neighborhood_id) REFERENCES neighborhoods(id)
                    )
                    """
                )

                db.execSQL(
                    """
                    INSERT INTO orders_new(id, customer_id, driver_id, neighborhood_id, amount, description, date_time, settled, status)
                    SELECT o.id,
                           COALESCE(c.id, (SELECT id FROM customers WHERE name='__UNKNOWN__' LIMIT 1)),
                           COALESCE(d.id, (SELECT id FROM drivers WHERE name='__UNKNOWN__' LIMIT 1)),
                           COALESCE(n.id, (SELECT id FROM neighborhoods WHERE name='__UNKNOWN__' LIMIT 1)),
                           o.amount, o.description, o.date_time, o.settled, o.status
                    FROM orders o
                    LEFT JOIN customers c ON c.name = o.customer
                    LEFT JOIN drivers d ON d.name = o.driver
                    LEFT JOIN neighborhoods n ON n.name = o.neighborhood
                    """
                )

                db.execSQL("DROP TABLE orders")
                db.execSQL("ALTER TABLE orders_new RENAME TO orders")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_orders_driver_id ON orders(driver_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_orders_neighborhood_id ON orders(neighborhood_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(date_time)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_orders_settled ON orders(settled)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS payments_new(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        driver_id INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        payment_method TEXT NOT NULL,
                        date_time TEXT NOT NULL,
                        FOREIGN KEY(driver_id) REFERENCES drivers(id)
                    )
                    """
                )

                db.execSQL(
                    """
                    INSERT INTO payments_new(id, driver_id, amount, payment_method, date_time)
                    SELECT p.id,
                           COALESCE(d.id, (SELECT id FROM drivers WHERE name='__UNKNOWN__' LIMIT 1)),
                           p.amount, p.payment_method, p.date_time
                    FROM payments p
                    LEFT JOIN drivers d ON d.name = p.driver
                    """
                )

                db.execSQL("DROP TABLE payments")
                db.execSQL("ALTER TABLE payments_new RENAME TO payments")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS commission_history_new(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        driver_id INTEGER NOT NULL,
                        old_commission REAL NOT NULL,
                        new_commission REAL NOT NULL,
                        date_time TEXT NOT NULL,
                        FOREIGN KEY(driver_id) REFERENCES drivers(id)
                    )
                    """
                )

                db.execSQL(
                    """
                    INSERT INTO commission_history_new(id, driver_id, old_commission, new_commission, date_time)
                    SELECT ch.id,
                           COALESCE(d.id, (SELECT id FROM drivers WHERE name='__UNKNOWN__' LIMIT 1)),
                           ch.old_commission, ch.new_commission, ch.date_time
                    FROM commission_history ch
                    LEFT JOIN drivers d ON d.name = ch.driver
                    """
                )

                db.execSQL("DROP TABLE commission_history")
                db.execSQL("ALTER TABLE commission_history_new RENAME TO commission_history")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE orders ADD COLUMN settled_at TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_orders_settled_at ON orders(settled_at)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "DeliveryDB"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
