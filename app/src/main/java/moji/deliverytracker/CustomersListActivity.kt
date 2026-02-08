package moji.deliverytracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomersListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerAdapter
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvEmpty: TextView
    private lateinit var shimmer: ShimmerFrameLayout
    private var allCustomers = listOf<Customer>()
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customers_list)

        supportActionBar?.title = getString(R.string.customers_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getInstance(this)
        recyclerView = findViewById(R.id.recyclerCustomers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        etSearch = findViewById(R.id.etSearchCustomer)
        tvEmpty = findViewById(R.id.tvEmptyCustomers)
        shimmer = findViewById(R.id.shimmerCustomers)

        adapter = CustomerAdapter(emptyList()) { customer, action ->
            when (action) {
                "edit" -> {
                    val intent = Intent(this, AddCustomerActivity::class.java).apply {
                        putExtra("customer_id", customer.id)
                    }
                    startActivity(intent)
                }
                "delete" -> confirmDelete(customer)
            }
        }
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterCustomers() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<FloatingActionButton>(R.id.fabAddCustomer).setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        observeCustomers()
    }

    private fun observeCustomers() {
        shimmer.startShimmer()
        lifecycleScope.launch {
            db.customerDao().getAllFlow().collectLatest { customers ->
                if (firstLoad) {
                    firstLoad = false
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                }
                allCustomers = customers
                filterCustomers()
            }
        }
    }

    private fun filterCustomers() {
        val query = etSearch.text.toString().lowercase()
        val filtered = if (query.isEmpty()) allCustomers else allCustomers.filter {
            it.name.lowercase().contains(query) ||
                it.nationalId.contains(query) ||
                it.phone.contains(query)
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun confirmDelete(customer: Customer) {
        lifecycleScope.launch {
            val hasOrders = EntityHelper.customerHasOrders(db, customer.id)

            if (hasOrders) {
                Toast.makeText(
                    this@CustomersListActivity,
                    getString(R.string.delete_customer_has_orders),
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            AlertDialog.Builder(this@CustomersListActivity)
                .setTitle(getString(R.string.delete_customer_title))
                .setMessage(getString(R.string.delete_customer_message, customer.name))
                .setPositiveButton(getString(R.string.action_yes)) { _, _ ->
                    lifecycleScope.launch {
                        val success = db.customerDao().deleteById(customer.id) > 0
                        if (success) {
                            Toast.makeText(this@CustomersListActivity, getString(R.string.delete_customer_success), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@CustomersListActivity, getString(R.string.delete_customer_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton(getString(R.string.action_no), null)
                .show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
