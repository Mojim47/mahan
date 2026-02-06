package moji.deliverytracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddDriverActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase
    private var driverId: Int = -1
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_driver)

        db = AppDatabase.getInstance(this)

        val etName = findViewById<TextInputEditText>(R.id.etDriverName)
        val etNationalId = findViewById<TextInputEditText>(R.id.etDriverNationalId)
        val etPlate = findViewById<TextInputEditText>(R.id.etDriverPlate)
        val etPhone = findViewById<TextInputEditText>(R.id.etDriverPhone)
        val etAddress = findViewById<TextInputEditText>(R.id.etDriverAddress)
        val etCommission = findViewById<TextInputEditText>(R.id.etDriverCommission)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveDriver)

        intent?.let {
            driverId = it.getIntExtra("driver_id", -1)
            if (driverId != -1) {
                isEditMode = true
                supportActionBar?.title = getString(R.string.edit_driver_title)
                etName.setText(it.getStringExtra("driver_name"))
                etNationalId.setText(it.getStringExtra("driver_national_id"))
                etPlate.setText(it.getStringExtra("driver_plate"))
                etPhone.setText(it.getStringExtra("driver_phone"))
                etAddress.setText(it.getStringExtra("driver_address"))
                etCommission.setText(it.getFloatExtra("driver_commission", 0f).toString())
            } else {
                supportActionBar?.title = getString(R.string.add_driver_title)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val nationalId = etNationalId.text.toString().trim()
            val plate = etPlate.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val commission = etCommission.text.toString().toFloatOrNull() ?: 0f

            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.name_required_driver), Toast.LENGTH_SHORT).show()
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

            if (!Validator.isValidPlate(plate)) {
                Toast.makeText(this, getString(R.string.plate_invalid), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (commission < 0 || commission > 100) {
                Toast.makeText(this, getString(R.string.commission_invalid), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                lifecycleScope.launch {
                    val success = db.withTransaction {
                        val existing = db.driverDao().getById(driverId) ?: return@withTransaction false
                        if (existing.commission != commission) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            db.commissionHistoryDao().insert(
                                CommissionHistory(
                                    driverId = existing.id,
                                    oldCommission = existing.commission,
                                    newCommission = commission,
                                    dateTime = sdf.format(Date())
                                )
                            )
                        }
                        val updated = existing.copy(
                            name = name,
                            nationalId = nationalId,
                            plate = plate,
                            phone = phone,
                            address = address,
                            commission = commission
                        )
                        db.driverDao().update(updated) > 0
                    }
                    if (success) {
                        Toast.makeText(this@AddDriverActivity, getString(R.string.driver_updated), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddDriverActivity, getString(R.string.driver_update_error), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                lifecycleScope.launch {
                    val result = db.driverDao().insert(
                        Driver(
                            name = name,
                            nationalId = nationalId,
                            plate = plate,
                            phone = phone,
                            address = address,
                            commission = commission
                        )
                    )
                    if (result != -1L) {
                        Toast.makeText(this@AddDriverActivity, getString(R.string.driver_added), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddDriverActivity, getString(R.string.name_duplicate_error), Toast.LENGTH_SHORT).show()
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
