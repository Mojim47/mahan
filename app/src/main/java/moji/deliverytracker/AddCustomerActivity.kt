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
                etName.setText(it.getStringExtra("customer_name"))
                etNationalId.setText(it.getStringExtra("customer_national_id"))
                etPhone.setText(it.getStringExtra("customer_phone"))
                etAddress.setText(it.getStringExtra("customer_address"))
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

            if (isEditMode) {
                lifecycleScope.launch {
                    val existing = db.customerDao().getById(customerId)
                    if (existing == null) {
                        Toast.makeText(this@AddCustomerActivity, getString(R.string.customer_update_error), Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val updated = existing.copy(
                        name = name,
                        nationalId = nationalId,
                        phone = phone,
                        address = address
                    )
                    val success = db.customerDao().update(updated) > 0
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
