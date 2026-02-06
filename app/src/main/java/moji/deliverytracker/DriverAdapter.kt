package moji.deliverytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class DriverAdapter(
    private val drivers: List<Driver>,
    private val onAction: (Driver, String) -> Unit
) : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDriverName)
        val tvDetails: TextView = view.findViewById(R.id.tvDriverDetails)
        val tvCommission: TextView = view.findViewById(R.id.tvDriverCommission)
        val card: MaterialCardView = view.findViewById(R.id.cardDriver)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_driver, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val driver = drivers[position]
        holder.tvName.text = driver.name

        val ctx = holder.itemView.context
        val details = buildString {
            if (driver.nationalId.isNotEmpty()) append(ctx.getString(R.string.label_national_id) + ": ${driver.nationalId}\n")
            if (driver.plate.isNotEmpty()) append(ctx.getString(R.string.label_plate) + ": ${driver.plate}\n")
            if (driver.phone.isNotEmpty()) append(ctx.getString(R.string.label_phone) + ": ${driver.phone}\n")
            if (driver.address.isNotEmpty()) append(ctx.getString(R.string.label_address) + ": ${driver.address}")
        }
        holder.tvDetails.text = details.ifEmpty { ctx.getString(R.string.details_empty) }
        holder.tvCommission.text = ctx.getString(R.string.item_commission_format, driver.commission.toString())

        holder.card.setOnClickListener { onAction(driver, "edit") }
        holder.card.setOnLongClickListener {
            onAction(driver, "delete")
            true
        }
    }

    override fun getItemCount() = drivers.size
}
