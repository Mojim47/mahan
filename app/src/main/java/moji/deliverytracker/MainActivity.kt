package moji.deliverytracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var etCustomer: AutoCompleteTextView
    private lateinit var etDriver: AutoCompleteTextView
    private lateinit var etNeighborhood: AutoCompleteTextView
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnSelectDate: MaterialButton
    private var selectedDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.title = getString(R.string.title_new_order)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = AppDatabase.getInstance(this)
        NotificationHelper.createNotificationChannel(this)
        initViews()
        setupAutoComplete()

        btnSelectDate.setOnClickListener { openDatePicker() }
        btnSubmit.setOnClickListener { submitOrder() }
    }

    private fun initViews() {
        etCustomer = findViewById(R.id.etCustomer)
        etDriver = findViewById(R.id.etDriver)
        etNeighborhood = findViewById(R.id.etNeighborhood)
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnSelectDate = findViewById(R.id.btnSelectDate)
    }

    private fun setupAutoComplete() {
        lifecycleScope.launch {
            db.customerDao().getNamesFlow().collectLatest { customers ->
                etCustomer.setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, customers))
            }
        }
        lifecycleScope.launch {
            db.driverDao().getNamesFlow().collectLatest { drivers ->
                etDriver.setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, drivers))
            }
        }
        lifecycleScope.launch {
            db.neighborhoodDao().getNamesFlow().collectLatest { neighborhoods ->
                etNeighborhood.setAdapter(ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, neighborhoods))
            }
        }
    }

    private fun openDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.date_picker_title))
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val inputSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val displaySdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val selected = Date(selection)
            selectedDate = inputSdf.format(selected)
            btnSelectDate.text = getString(R.string.date_selected_format, displaySdf.format(selected))
        }

        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun submitOrder() {
        val customerName = etCustomer.text.toString().trim()
        val driverName = etDriver.text.toString().trim()
        val neighborhoodName = etNeighborhood.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()

        val errors = mutableListOf<String>()
        if (customerName.isEmpty()) errors.add(getString(R.string.error_customer_required))
        if (driverName.isEmpty()) errors.add(getString(R.string.error_driver_required))
        if (neighborhoodName.isEmpty()) errors.add(getString(R.string.error_neighborhood_required))
        if (amountText.isEmpty()) errors.add(getString(R.string.error_amount_required))

        val amount = amountText.toIntOrNull()
        if (amount == null && amountText.isNotEmpty()) errors.add(getString(R.string.error_amount_number))
        if (amount != null && amount < 0) errors.add(getString(R.string.error_amount_negative))

        if (errors.isNotEmpty()) {
            Toast.makeText(this, errors.joinToString("\n"), Toast.LENGTH_LONG).show()
            return
        }

        btnSubmit.isEnabled = false
        lifecycleScope.launch {
            val customerId = db.customerDao().getIdByName(customerName)
            val driverId = db.driverDao().getIdByName(driverName)
            val neighborhoodId = db.neighborhoodDao().getIdByName(neighborhoodName)

            if (customerId == null || driverId == null || neighborhoodId == null) {
                btnSubmit.isEnabled = true
                Toast.makeText(this@MainActivity, getString(R.string.order_saved_error), Toast.LENGTH_SHORT).show()
                return@launch
            }

            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val order = Order(
                customerId = customerId,
                driverId = driverId,
                neighborhoodId = neighborhoodId,
                amount = amount!!,
                description = description,
                dateTime = selectedDate ?: now,
                settled = false,
                settledAt = null
            )
            val result = db.orderDao().insert(order)
            btnSubmit.isEnabled = true
            if (result != -1L) {
                NotificationHelper.showOrderNotification(this@MainActivity, getString(R.string.order_new_title), getString(R.string.order_new_message, customerName))
                Toast.makeText(this@MainActivity, getString(R.string.order_saved_success), Toast.LENGTH_SHORT).show()
                clearForm()
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.order_saved_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearForm() {
        etCustomer.setText("")
        etDriver.setText("")
        etNeighborhood.setText("")
        etAmount.setText("")
        etDescription.setText("")
        selectedDate = null
        btnSelectDate.text = getString(R.string.date_select_label)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
