package moji.deliverytracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(
    private var orders: MutableList<Order>,
    private val context: Context,
    private val onItemClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    fun updateList(newList: MutableList<Order>) {
        orders.clear()
        orders.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)

        holder.itemView.setOnLongClickListener {
            onItemClick(order)
            true
        }
    }

    override fun getItemCount() = orders.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomer = itemView.findViewById<TextView>(R.id.tvCustomer)
        private val tvDriver = itemView.findViewById<TextView>(R.id.tvDriver)
        private val tvAmount = itemView.findViewById<TextView>(R.id.tvAmount)
        private val tvDateTime = itemView.findViewById<TextView>(R.id.tvDateTime)
        private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)

        fun bind(order: Order) {
            tvCustomer.text = order.customer
            tvDriver.text = order.driver
            tvAmount.text = CurrencyFormatter.formatToman(order.amount, context.getString(R.string.toman))
            tvDateTime.text = order.dateTime

            if (order.settled) {
                tvStatus.text = context.getString(R.string.orders_status_settled)
                tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_primary))
            } else {
                tvStatus.text = context.getString(R.string.orders_status_unsettled)
                tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_error))
            }
        }
    }
}
