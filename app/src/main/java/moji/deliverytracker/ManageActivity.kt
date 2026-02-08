package moji.deliverytracker

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var shimmer: ShimmerFrameLayout
    private val neighborhoods = mutableListOf<String>()
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)

        supportActionBar?.title = getString(R.string.neighborhoods_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getInstance(this)
        listView = findViewById(R.id.listNeighborhoods)
        shimmer = findViewById(R.id.shimmerNeighborhoods)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, neighborhoods)
        listView.adapter = adapter

        findViewById<ExtendedFloatingActionButton>(R.id.btnAddNeighborhood).setOnClickListener {
            showAddDialog()
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            confirmDelete(neighborhoods[position])
            true
        }

        observeNeighborhoods()
    }

    private fun observeNeighborhoods() {
        shimmer.startShimmer()
        lifecycleScope.launch {
            db.neighborhoodDao().getNamesFlow().collectLatest { list ->
                if (firstLoad) {
                    firstLoad = false
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                }
                neighborhoods.clear()
                neighborhoods.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddDialog() {
        val input = TextInputEditText(this)
        input.hint = getString(R.string.neighborhood_name_hint)
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(50))

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.neighborhood_add_title))
            .setView(input)
            .setPositiveButton(getString(R.string.neighborhood_add_button)) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        val success = db.neighborhoodDao().insert(Neighborhood(name = name)) != -1L
                        if (success) {
                            Toast.makeText(this@ManageActivity, getString(R.string.neighborhood_added), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ManageActivity, getString(R.string.name_duplicate_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }

    private fun confirmDelete(name: String) {
        lifecycleScope.launch {
            val neighborhoodId = db.neighborhoodDao().getIdByName(name)
            if (neighborhoodId != null && EntityHelper.neighborhoodHasOrders(db, neighborhoodId)) {
                Toast.makeText(
                    this@ManageActivity,
                    getString(R.string.delete_neighborhood_has_orders),
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            AlertDialog.Builder(this@ManageActivity)
                .setTitle(getString(R.string.neighborhood_delete_title))
                .setMessage(getString(R.string.neighborhood_delete_message, name))
                .setPositiveButton(getString(R.string.action_yes)) { _, _ ->
                    lifecycleScope.launch {
                        val success = db.neighborhoodDao().deleteByName(name) > 0
                        if (success) {
                            Toast.makeText(this@ManageActivity, getString(R.string.neighborhood_delete_success), Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ManageActivity, getString(R.string.neighborhood_delete_error), Toast.LENGTH_SHORT).show()
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
