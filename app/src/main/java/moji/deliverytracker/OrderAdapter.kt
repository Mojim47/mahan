package moji.deliverytracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class OrderAdapter(
    private var orders: MutableList<OrderWithNames>,
    private val context: Context,
    private val onItemClick: (OrderWithNames) -> Unit
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    fun updateList(newList: MutableList<OrderWithNames>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = orders.size
            override fun getNewListSize() = newList.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = orders[oldPos].id == newList[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = orders[oldPos] == newList[newPos]
        })
        orders.clear()
        orders.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
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

        fun bind(order: OrderWithNames) {
            tvCustomer.text = order.customerName
            tvDriver.text = order.driverName
            tvAmount.text = CurrencyFormatter.formatToman(order.amount, context.getString(R.string.toman))
            tvDateTime.text = order.dateTime

            if (order.settled) {
                tvStatus.text = context.getString(R.string.orders_status_settled)
                ViewCompat.setBackgroundTintList(
                    tvStatus,
                    ContextCompat.getColorStateList(context, R.color.color_success)
                )
            } else {
                tvStatus.text = context.getString(R.string.orders_status_unsettled)
                ViewCompat.setBackgroundTintList(
                    tvStatus,
                    ContextCompat.getColorStateList(context, R.color.md_theme_error)
                )
            }
        }
    }
}
