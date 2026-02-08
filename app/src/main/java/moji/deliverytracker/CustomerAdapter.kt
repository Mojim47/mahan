package moji.deliverytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class CustomerAdapter(
    private var customers: List<Customer>,
    private val onAction: (Customer, String) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    fun updateList(newList: List<Customer>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = customers.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = customers[oldPos].id == newList[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = customers[oldPos] == newList[newPos]
        })
        customers = newList
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvDetails: TextView = view.findViewById(R.id.tvCustomerDetails)
        val tvInitial: TextView = view.findViewById(R.id.tvCustomerInitial)
        val card: MaterialCardView = view.findViewById(R.id.cardCustomer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]
        holder.tvName.text = customer.name
        holder.tvInitial.text = customer.name.firstOrNull()?.uppercase() ?: ""

        val ctx = holder.itemView.context
        val details = buildString {
            if (customer.phone.isNotEmpty()) append(customer.phone)
            if (customer.address.isNotEmpty()) {
                if (isNotEmpty()) append(" • ")
                append(customer.address)
            }
        }
        holder.tvDetails.text = details.ifEmpty { ctx.getString(R.string.details_empty) }

        holder.card.setOnClickListener { onAction(customer, "edit") }
        holder.card.setOnLongClickListener {
            onAction(customer, "delete")
            true
        }
    }

    override fun getItemCount() = customers.size
}
