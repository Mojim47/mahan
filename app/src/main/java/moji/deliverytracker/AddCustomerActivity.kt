package moji.deliverytracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AddCustomerActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var customerId: Int = -1
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_customer)

        db = AppDatabase.getInstance(this)

        val etName = findViewById<TextInputEditText>(R.id.etCustomerName)
        val etNationalId = findViewById<TextInputEditText>(R.id.etCustomerNationalId)
        val etPhone = findViewById<TextInputEditText>(R.id.etCustomerPhone)
        val etAddress = findViewById<TextInputEditText>(R.id.etCustomerAddress)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveCustomer)

        intent?.let {
            customerId = it.getIntExtra("customer_id", -1)
            if (customerId != -1) {
                isEditMode = true
                supportActionBar?.title = getString(R.string.edit_customer_title)
                loadCustomerData(etName, etNationalId, etPhone, etAddress)
            } else {
                supportActionBar?.title = getString(R.string.add_customer_title)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val nationalId = etNationalId.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.name_required_customer), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Validator.isValidNationalId(nationalId)) {
                Toast.makeText(this, getString(R.string.national_id_invalid), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Validator.isValidPhone(phone)) {
                Toast.makeText(this, getString(R.string.phone_invalid), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false

            if (isEditMode) {
                lifecycleScope.launch {
                    val existing = db.customerDao().getById(customerId)
                    if (existing == null) {
                        btnSave.isEnabled = true
                        Toast.makeText(this@AddCustomerActivity, getString(R.string.customer_update_error), Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val conflictingId = db.customerDao().getIdByName(name)
                    if (conflictingId != null && conflictingId != existing.id) {
                        btnSave.isEnabled = true
                        Toast.makeText(this@AddCustomerActivity, getString(R.string.name_duplicate_error), Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val updated = existing.copy(
                        name = name,
                        nationalId = nationalId,
                        phone = phone,
                        address = address
                    )
                    val success = db.customerDao().update(updated) > 0
                    btnSave.isEnabled = true
                    if (success) {
                        Toast.makeText(this@AddCustomerActivity, getString(R.string.customer_updated), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddCustomerActivity, getString(R.string.customer_update_error), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                lifecycleScope.launch {
                    val result = db.customerDao().insert(
                        Customer(
                            name = name,
                            nationalId = nationalId,
                            phone = phone,
                            address = address
                        )
                    )
                    btnSave.isEnabled = true
                    if (result != -1L) {
                        Toast.makeText(this@AddCustomerActivity, getString(R.string.customer_added), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddCustomerActivity, getString(R.string.name_duplicate_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadCustomerData(
        etName: TextInputEditText,
        etNationalId: TextInputEditText,
        etPhone: TextInputEditText,
        etAddress: TextInputEditText
    ) {
        lifecycleScope.launch {
            val customer = db.customerDao().getById(customerId)
            if (customer != null) {
                etName.setText(customer.name)
                etNationalId.setText(customer.nationalId)
                etPhone.setText(customer.phone)
                etAddress.setText(customer.address)
            } else {
                Toast.makeText(this@AddCustomerActivity, getString(R.string.customer_update_error), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
