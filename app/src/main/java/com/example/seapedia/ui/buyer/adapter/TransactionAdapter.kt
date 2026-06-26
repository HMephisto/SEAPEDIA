package com.example.seapedia.ui.buyer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.seapedia.data.model.Transaction
import com.example.seapedia.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvDescription.text = transaction.description
            binding.tvType.text = transaction.type
            binding.tvAmount.text = formatAmount(transaction.amount, transaction.type)
            binding.tvDate.text = formatDate(transaction.createdAt)

            val isCredit = transaction.type == "TOPUP"
            binding.tvAmount.setTextColor(
                binding.root.context.getColor(
                    if (isCredit) com.example.seapedia.R.color.success
                    else com.example.seapedia.R.color.warning
                )
            )

            binding.tvType.setBackgroundResource(
                if (isCredit) com.example.seapedia.R.drawable.bg_tag_green
                else com.example.seapedia.R.drawable.bg_tag_red
            )
        }

        private fun formatAmount(amount: String, type: String): String {
            val value = amount.toDoubleOrNull() ?: 0.0
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val prefix = if (type == "TOPUP") "+" else "-"
            return "$prefix${format.format(value)}"
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date!!)
            } catch (e: Exception) {
                dateString
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem == newItem
    }
}