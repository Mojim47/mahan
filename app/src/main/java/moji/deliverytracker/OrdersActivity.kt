package moji.deliverytracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrdersActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var rvOrders: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvEmptyState: TextView
    private lateinit var btnFilterAll: MaterialButton
    private lateinit var btnFilterSettled: MaterialButton
    private lateinit var btnFilterUnsettled: MaterialButton
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var adapter: OrderAdapter
    private var allOrders = listOf<OrderWithNames>()
    private var currentFilter = "all"
    private var filterDriver: String? = null
    private var filterCustomer: String? = null
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        db = AppDatabase.getInstance(this)
        rvOrders = findViewById(R.id.rvOrders)
        etSearch = findViewById(R.id.etSearch)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        btnFilterAll = findViewById(R.id.btnFilterAll)
        btnFilterSettled = findViewById(R.id.btnFilterSettled)
        btnFilterUnsettled = findViewById(R.id.btnFilterUnsettled)
        shimmer = findViewById(R.id.shimmerOrders)

        adapter = OrderAdapter(mutableListOf(), this) { order ->
            val intent = Intent(this, EditOrderActivity::class.java)
            intent.putExtra("orderId", order.id)
            startActivity(intent)
        }
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = adapter

        setupSearch()
        setupFilters()
        observeOrders()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterOrders()
            }
        })
    }

    private fun setupFilters() {
        btnFilterAll.setOnClickListener {
            currentFilter = "all"
            updateFilterButtons()
            filterOrders()
        }

        btnFilterSettled.setOnClickListener {
            currentFilter = "settled"
            updateFilterButtons()
            filterOrders()
        }

        btnFilterUnsettled.setOnClickListener {
            currentFilter = "unsettled"
            updateFilterButtons()
            filterOrders()
        }
    }

    private fun updateFilterButtons() {
        btnFilterAll.icon = if (currentFilter == "all") resources.getDrawable(android.R.drawable.checkbox_on_background, theme) else null
        btnFilterSettled.icon = if (currentFilter == "settled") resources.getDrawable(android.R.drawable.checkbox_on_background, theme) else null
        btnFilterUnsettled.icon = if (currentFilter == "unsettled") resources.getDrawable(android.R.drawable.checkbox_on_background, theme) else null
    }

    private fun observeOrders() {
        shimmer.startShimmer()
        lifecycleScope.launch {
            db.orderDao().getAllWithNamesFlow().collectLatest { orders ->
                if (firstLoad) {
                    firstLoad = false
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                }
                allOrders = orders
                filterOrders()
            }
        }
    }

    private fun filterOrders() {
        val searchText = etSearch.text.toString().trim().lowercase()

        var filtered = when (currentFilter) {
            "settled" -> allOrders.filter { it.settled }
            "unsettled" -> allOrders.filter { !it.settled }
            else -> allOrders
        }

        if (searchText.isNotEmpty()) {
            filtered = filtered.filter {
                it.customerName.lowercase().contains(searchText) ||
                    it.driverName.lowercase().contains(searchText) ||
                    it.neighborhoodName.lowercase().contains(searchText)
            }
        }

        if (filterDriver != null) {
            filtered = filtered.filter { it.driverName == filterDriver }
        }

        if (filterCustomer != null) {
            filtered = filtered.filter { it.customerName == filterCustomer }
        }

        adapter.updateList(filtered.toMutableList())

        if (filtered.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            rvOrders.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvOrders.visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
