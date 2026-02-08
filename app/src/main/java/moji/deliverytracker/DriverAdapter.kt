package moji.deliverytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class DriverAdapter(
    private var drivers: List<Driver>,
    private val onAction: (Driver, String) -> Unit
) : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    fun updateList(newList: List<Driver>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = drivers.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = drivers[oldPos].id == newList[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = drivers[oldPos] == newList[newPos]
        })
        drivers = newList
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDriverName)
        val tvDetails: TextView = view.findViewById(R.id.tvDriverDetails)
        val tvCommission: TextView = view.findViewById(R.id.tvDriverCommission)
        val tvInitial: TextView = view.findViewById(R.id.tvDriverInitial)
        val card: MaterialCardView = view.findViewById(R.id.cardDriver)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_driver, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val driver = drivers[position]
        holder.tvName.text = driver.name
        holder.tvInitial.text = driver.name.firstOrNull()?.uppercase() ?: ""

        val ctx = holder.itemView.context
        val details = buildString {
            if (driver.phone.isNotEmpty()) append(driver.phone)
            if (driver.plate.isNotEmpty()) {
                if (isNotEmpty()) append(" • ")
                append(driver.plate)
            }
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
