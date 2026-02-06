package moji.deliverytracker

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditOrderActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var orderId: Int = -1
    private var currentOrder: OrderWithNames? = null

    private lateinit var etCustomer: AutoCompleteTextView
    private lateinit var etDriver: AutoCompleteTextView
    private lateinit var etNeighborhood: AutoCompleteTextView
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private val defaultCommission = 20f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_order)

        db = AppDatabase.getInstance(this)
        orderId = intent.extras?.getInt("orderId") ?: -1

        if (orderId == -1) {
            Toast.makeText(this, getString(R.string.order_load_error), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupAutoComplete()
        loadOrderData()
        setupClickListeners()
    }

    private fun initViews() {
        etCustomer = findViewById(R.id.etCustomer)
        etDriver = findViewById(R.id.etDriver)
        etNeighborhood = findViewById(R.id.etNeighborhood)
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupAutoComplete() {
        lifecycleScope.launch {
            db.customerDao().getNamesFlow().collectLatest { customers ->
                etCustomer.setAdapter(ArrayAdapter(this@EditOrderActivity, android.R.layout.simple_dropdown_item_1line, customers))
            }
        }
        lifecycleScope.launch {
            db.driverDao().getNamesFlow().collectLatest { drivers ->
                etDriver.setAdapter(ArrayAdapter(this@EditOrderActivity, android.R.layout.simple_dropdown_item_1line, drivers))
            }
        }
        lifecycleScope.launch {
            db.neighborhoodDao().getNamesFlow().collectLatest { neighborhoods ->
                etNeighborhood.setAdapter(ArrayAdapter(this@EditOrderActivity, android.R.layout.simple_dropdown_item_1line, neighborhoods))
            }
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            val customer = etCustomer.text.toString().trim()
            val driver = etDriver.text.toString().trim()
            val neighborhood = etNeighborhood.text.toString().trim()
            val amount = etAmount.text.toString().toIntOrNull()
            val description = etDescription.text.toString().trim()

            if (customer.isEmpty() || driver.isEmpty() || neighborhood.isEmpty() || amount == null) {
                Toast.makeText(this, getString(R.string.order_fields_required), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount < 0) {
                Toast.makeText(this, getString(R.string.amount_negative_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            lifecycleScope.launch {
                val existing = currentOrder
                if (existing == null) {
                    btnSave.isEnabled = true
                    Toast.makeText(this@EditOrderActivity, getString(R.string.order_update_error), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val ids = db.withTransaction {
                    val customerId = ensureCustomer(customer)
                    val driverId = ensureDriver(driver)
                    val neighborhoodId = ensureNeighborhood(neighborhood)
                    Triple(customerId, driverId, neighborhoodId)
                }
                val customerId = ids.first
                val driverId = ids.second
                val neighborhoodId = ids.third

                if (customerId == null || driverId == null || neighborhoodId == null) {
                    btnSave.isEnabled = true
                    Toast.makeText(this@EditOrderActivity, getString(R.string.order_update_error), Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val updated = existing.copy(
                    customerId = customerId,
                    driverId = driverId,
                    neighborhoodId = neighborhoodId,
                    amount = amount,
                    description = description
                )
                val order = Order(
                    id = updated.id,
                    customerId = updated.customerId,
                    driverId = updated.driverId,
                    neighborhoodId = updated.neighborhoodId,
                    amount = updated.amount,
                    description = updated.description,
                    dateTime = updated.dateTime,
                    settled = updated.settled,
                    settledAt = updated.settledAt,
                    status = updated.status
                )
                val success = db.orderDao().update(order) > 0
                btnSave.isEnabled = true
                if (success) {
                    Toast.makeText(this@EditOrderActivity, getString(R.string.order_updated), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditOrderActivity, getString(R.string.order_update_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.order_delete_title))
                .setMessage(getString(R.string.confirm_delete_message))
                .setPositiveButton(getString(R.string.action_yes)) { _, _ ->
                    lifecycleScope.launch {
                        val success = db.orderDao().deleteById(orderId) > 0
                        if (success) {
                            Toast.makeText(this@EditOrderActivity, getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@EditOrderActivity, getString(R.string.order_delete_error), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton(getString(R.string.action_no), null)
                .show()
        }
    }

    private fun loadOrderData() {
        lifecycleScope.launch {
            currentOrder = db.orderDao().getByIdWithNames(orderId)
            currentOrder?.let { order ->
                etCustomer.setText(order.customerName)
                etDriver.setText(order.driverName)
                etNeighborhood.setText(order.neighborhoodName)
                etAmount.setText(order.amount.toString())
                etDescription.setText(order.description)
            } ?: run {
                Toast.makeText(this@EditOrderActivity, getString(R.string.order_not_found), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private suspend fun ensureCustomer(name: String): Int? {
        val existing = db.customerDao().getIdByName(name)
        if (existing != null) return existing
        db.customerDao().insert(Customer(name = name, nationalId = "", phone = "", address = ""))
        return db.customerDao().getIdByName(name)
    }

    private suspend fun ensureDriver(name: String): Int? {
        val existing = db.driverDao().getIdByName(name)
        if (existing != null) return existing
        db.driverDao().insert(
            Driver(
                name = name,
                nationalId = "",
                plate = "",
                phone = "",
                address = "",
                commission = defaultCommission
            )
        )
        return db.driverDao().getIdByName(name)
    }

    private suspend fun ensureNeighborhood(name: String): Int? {
        val existing = db.neighborhoodDao().getIdByName(name)
        if (existing != null) return existing
        db.neighborhoodDao().insert(Neighborhood(name = name))
        return db.neighborhoodDao().getIdByName(name)
    }
}
