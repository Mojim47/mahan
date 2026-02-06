package moji.deliverytracker

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReportsActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var btnToday: MaterialButton
    private lateinit var btnWeek: MaterialButton
    private lateinit var btnMonth: MaterialButton
    private lateinit var tvTotalOrders: TextView
    private lateinit var tvTotalSales: TextView
    private lateinit var tvTotalCommission: TextView
    private lateinit var tvNetIncome: TextView
    private lateinit var tvSettled: TextView
    private lateinit var tvUnsettled: TextView
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var content: LinearLayout
    private var firstLoad = true

    private val periodFlow = MutableStateFlow(ReportPeriod.TODAY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        supportActionBar?.title = getString(R.string.report_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getInstance(this)
        initViews()
        setupClickListeners()
        observeReports()
    }

    private fun initViews() {
        btnToday = findViewById(R.id.btnToday)
        btnWeek = findViewById(R.id.btnWeek)
        btnMonth = findViewById(R.id.btnMonth)
        tvTotalOrders = findViewById(R.id.tvTotalOrders)
        tvTotalSales = findViewById(R.id.tvTotalSales)
        tvTotalCommission = findViewById(R.id.tvTotalCommission)
        tvNetIncome = findViewById(R.id.tvNetIncome)
        tvSettled = findViewById(R.id.tvSettled)
        tvUnsettled = findViewById(R.id.tvUnsettled)
        shimmer = findViewById(R.id.shimmerReports)
        content = findViewById(R.id.reportContent)
    }

    private fun setupClickListeners() {
        btnToday.setOnClickListener {
            updateButtonStates(ReportPeriod.TODAY)
            periodFlow.value = ReportPeriod.TODAY
        }
        btnWeek.setOnClickListener {
            updateButtonStates(ReportPeriod.WEEK)
            periodFlow.value = ReportPeriod.WEEK
        }
        btnMonth.setOnClickListener {
            updateButtonStates(ReportPeriod.MONTH)
            periodFlow.value = ReportPeriod.MONTH
        }
    }

    private fun updateButtonStates(selected: ReportPeriod) {
        btnToday.isChecked = selected == ReportPeriod.TODAY
        btnWeek.isChecked = selected == ReportPeriod.WEEK
        btnMonth.isChecked = selected == ReportPeriod.MONTH
    }

    private fun observeReports() {
        shimmer.startShimmer()
        lifecycleScope.launch {
            combine(
                periodFlow,
                db.orderDao().getAllFlow(),
                db.driverDao().getAllFlow()
            ) { period, orders, drivers ->
                val filtered = ReportCalculator.filterOrders(orders, period)
                ReportCalculator.calculateSummary(filtered, drivers)
            }.collectLatest { summary ->
                if (firstLoad) {
                    firstLoad = false
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                    content.visibility = View.VISIBLE
                }
                val label = getString(R.string.toman)
                tvTotalOrders.text = summary.totalOrders.toString()
                tvTotalSales.text = CurrencyFormatter.formatToman(summary.totalSales, label)
                tvTotalCommission.text = CurrencyFormatter.formatToman(summary.totalCommission, label)
                tvNetIncome.text = CurrencyFormatter.formatToman(summary.netIncome, label)
                tvSettled.text = CurrencyFormatter.formatToman(summary.settled, label)
                tvUnsettled.text = CurrencyFormatter.formatToman(summary.unsettled, label)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
