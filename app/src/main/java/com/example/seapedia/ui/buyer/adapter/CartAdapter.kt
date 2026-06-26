package com.example.seapedia.ui.buyer.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.seapedia.data.model.CartItem
import com.example.seapedia.databinding.ItemCartBinding
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(private val onIncrease: (CartItem) -> Unit,
                  private val onDecrease: (CartItem) -> Unit,
                  private val onRemove: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(DiffCallback()) {

    var updatingItemId: Int? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
            val isUpdating = updatingItemId == item.id

            binding.tvProductName.text = item.productName
            binding.tvPrice.text = "Rp ${formatter.format(item.price.toDoubleOrNull() ?: 0.0)}"
            binding.tvSubtotal.text = "Rp ${formatter.format(item.subtotal)}"

            // Show spinner or quantity depending on loading state
            if (isUpdating) {
                binding.tvQuantity.visibility = View.INVISIBLE
                binding.progressQuantity.visibility = View.VISIBLE
            } else {
                binding.tvQuantity.visibility = View.VISIBLE
                binding.tvQuantity.text = item.quantity.toString()
                binding.progressQuantity.visibility = View.GONE
            }

            binding.btnIncrease.isEnabled = !isUpdating
            binding.btnDecrease.isEnabled = !isUpdating
            binding.btnRemove.isEnabled = !isUpdating

            binding.btnIncrease.setOnClickListener { onIncrease(item) }
            binding.btnDecrease.setOnClickListener { onDecrease(item) }
            binding.btnRemove.setOnClickListener { onRemove(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
    }
}