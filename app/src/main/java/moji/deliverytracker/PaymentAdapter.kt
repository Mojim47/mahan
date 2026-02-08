package moji.deliverytracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class PaymentAdapter(private val payments: MutableList<PaymentWithDriverName>) : RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDriver: TextView = view.findViewById(R.id.tvPaymentDriver)
        val tvAmount: TextView = view.findViewById(R.id.tvPaymentAmount)
        val tvMethod: TextView = view.findViewById(R.id.tvPaymentMethod)
        val tvDate: TextView = view.findViewById(R.id.tvPaymentDate)
    }

    fun updateList(newItems: List<PaymentWithDriverName>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = payments.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) = payments[oldPos].id == newItems[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int) = payments[oldPos] == newItems[newPos]
        })
        payments.clear()
        payments.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payment = payments[position]
        holder.tvDriver.text = payment.driverName
        holder.tvAmount.text = CurrencyFormatter.formatToman(payment.amount, holder.itemView.context.getString(R.string.toman))
        holder.tvMethod.text = payment.method
        holder.tvDate.text = payment.dateTime
    }

    override fun getItemCount() = payments.size
}
