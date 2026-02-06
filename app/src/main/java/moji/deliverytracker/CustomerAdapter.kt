package moji.deliverytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class CustomerAdapter(
    private val customers: List<Customer>,
    private val onAction: (Customer, String) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvDetails: TextView = view.findViewById(R.id.tvCustomerDetails)
        val card: MaterialCardView = view.findViewById(R.id.cardCustomer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]
        holder.tvName.text = customer.name

        val ctx = holder.itemView.context
        val details = buildString {
            if (customer.nationalId.isNotEmpty()) append(ctx.getString(R.string.label_national_id) + ": ${customer.nationalId}\n")
            if (customer.phone.isNotEmpty()) append(ctx.getString(R.string.label_phone) + ": ${customer.phone}\n")
            if (customer.address.isNotEmpty()) append(ctx.getString(R.string.label_address) + ": ${customer.address}")
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
