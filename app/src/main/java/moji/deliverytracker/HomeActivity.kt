package moji.deliverytracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        supportActionBar?.title = getString(R.string.home_app_title)

        db = AppDatabase.getInstance(this)

        findViewById<MaterialCardView>(R.id.cardNewOrder).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardOrders).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardDrivers).setOnClickListener {
            startActivity(Intent(this, DriversListActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardCustomers).setOnClickListener {
            startActivity(Intent(this, CustomersListActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardNeighborhoods).setOnClickListener {
            startActivity(Intent(this, ManageActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardSettle).setOnClickListener {
            startActivity(Intent(this, SettleActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardPayments).setOnClickListener {
            startActivity(Intent(this, PaymentsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardReports).setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardBackup)?.setOnClickListener {
            lifecycleScope.launch {
                val (success, message) = BackupHelper.exportToCSV(this@HomeActivity, db)
                val text = if (success) {
                    getString(R.string.backup_success, message)
                } else {
                    getString(R.string.backup_error, message)
                }
                Toast.makeText(this@HomeActivity, text, Toast.LENGTH_LONG).show()
            }
        }

        observeStats()
    }

    private fun observeStats() {
        val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) + "%"

        lifecycleScope.launch {
            db.orderDao().getSummaryFlow(todayPrefix).collectLatest { summary ->
                findViewById<TextView>(R.id.tvTodayTotal).text = getString(
                    R.string.home_today_total_format,
                    CurrencyFormatter.formatNumber(summary.total.toLong())
                )
                findViewById<TextView>(R.id.tvTodayCount).text = getString(R.string.home_today_count_format, summary.count)
            }
        }
        lifecycleScope.launch {
            db.driverDao().getCountFlow().collectLatest { count ->
                findViewById<TextView>(R.id.tvDriverCount)?.text = getString(R.string.home_drivers_format, count)
            }
        }
        lifecycleScope.launch {
            db.customerDao().getCountFlow().collectLatest { count ->
                findViewById<TextView>(R.id.tvCustomerCount)?.text = getString(R.string.home_customers_format, count)
            }
        }
        lifecycleScope.launch {
            db.neighborhoodDao().getCountFlow().collectLatest { count ->
                findViewById<TextView>(R.id.tvNeighborhoodCount)?.text = getString(R.string.home_neighborhoods_format, count)
            }
        }
    }
}
