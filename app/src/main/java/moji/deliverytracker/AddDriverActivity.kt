package moji.deliverytracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

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
                loadDriverData(etName, etNationalId, etPlate, etPhone, etAddress, etCommission)
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

            btnSave.isEnabled = false

            if (isEditMode) {
                lifecycleScope.launch {
                    val updateResult = db.withTransaction {
                        val existing = db.driverDao().getById(driverId) ?: return@withTransaction 0
                        if (existing.commission != commission) {
                            db.commissionHistoryDao().insert(
                                CommissionHistory(
                                    driverId = existing.id,
                                    oldCommission = existing.commission,
                                    newCommission = commission,
                                    dateTime = DateTimeUtils.nowDb()
                                )
                            )
                        }
                        val conflictingId = db.driverDao().getIdByName(name)
                        if (conflictingId != null && conflictingId != existing.id) {
                            return@withTransaction -1
                        }
                        val updated = existing.copy(
                            name = name,
                            nationalId = nationalId,
                            plate = plate,
                            phone = phone,
                            address = address,
                            commission = commission
                        )
                        db.driverDao().update(updated)
                    }
                    btnSave.isEnabled = true
                    if (updateResult > 0) {
                        Toast.makeText(this@AddDriverActivity, getString(R.string.driver_updated), Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (updateResult == -1) {
                        Toast.makeText(this@AddDriverActivity, getString(R.string.name_duplicate_error), Toast.LENGTH_SHORT).show()
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
                    btnSave.isEnabled = true
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

    private fun loadDriverData(
        etName: TextInputEditText,
        etNationalId: TextInputEditText,
        etPlate: TextInputEditText,
        etPhone: TextInputEditText,
        etAddress: TextInputEditText,
        etCommission: TextInputEditText
    ) {
        lifecycleScope.launch {
            val driver = db.driverDao().getById(driverId)
            if (driver != null) {
                etName.setText(driver.name)
                etNationalId.setText(driver.nationalId)
                etPlate.setText(driver.plate)
                etPhone.setText(driver.phone)
                etAddress.setText(driver.address)
                etCommission.setText(driver.commission.toString())
            } else {
                Toast.makeText(this@AddDriverActivity, getString(R.string.driver_update_error), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
