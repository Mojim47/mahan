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

class DriversListActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DriverAdapter
    private lateinit var etSearch: TextInputEditText
    private lateinit var tvEmpty: TextView
    private lateinit var shimmer: ShimmerFrameLayout
    private var allDrivers = listOf<Driver>()
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers_list)

        supportActionBar?.title = getString(R.string.drivers_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getInstance(this)
        recyclerView = findViewById(R.id.recyclerDrivers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        etSearch = findViewById(R.id.etSearchDriver)
        tvEmpty = findViewById(R.id.tvEmptyDrivers)
        shimmer = findViewById(R.id.shimmerDrivers)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterDrivers() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<FloatingActionButton>(R.id.fabAddDriver).setOnClickListener {
            startActivity(Intent(this, AddDriverActivity::class.java))
        }

        observeDrivers()
    }

    private fun observeDrivers() {
        shimmer.startShimmer()
        lifecycleScope.launch {
            db.driverDao().getAllFlow().collectLatest { drivers ->
                if (firstLoad) {
                    firstLoad = false
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                }
                allDrivers = drivers
                filterDrivers()
            }
        }
    }

    private fun filterDrivers() {
        val query = etSearch.text.toString().lowercase()
        val filtered = if (query.isEmpty()) allDrivers else allDrivers.filter {
            it.name.lowercase().contains(query) ||
                it.nationalId.contains(query) ||
                it.plate.lowercase().contains(query) ||
                it.phone.contains(query)
        }

        adapter = DriverAdapter(filtered) { driver, action ->
            when (action) {
                "edit" -> {
                    val intent = Intent(this, AddDriverActivity::class.java).apply {
                        putExtra("driver_id", driver.id)
                        putExtra("driver_name", driver.name)
                        putExtra("driver_national_id", driver.nationalId)
                        putExtra("driver_plate", driver.plate)
                        putExtra("driver_phone", driver.phone)
                        putExtra("driver_address", driver.address)
                        putExtra("driver_commission", driver.commission)
                    }
                    startActivity(intent)
                }
                "delete" -> confirmDelete(driver)
            }
        }
        recyclerView.adapter = adapter

        if (filtered.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun confirmDelete(driver: Driver) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_driver_title))
            .setMessage(getString(R.string.delete_driver_message, driver.name))
            .setPositiveButton(getString(R.string.action_yes)) { _, _ ->
                lifecycleScope.launch {
                    val success = db.driverDao().deleteByName(driver.name) > 0
                    if (success) {
                        Toast.makeText(this@DriversListActivity, getString(R.string.delete_driver_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@DriversListActivity, getString(R.string.delete_driver_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.action_no), null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
