package com.example.seapedia.ui.seller.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seapedia.data.model.Product
import com.example.seapedia.data.utils.Constants
import com.example.seapedia.databinding.ItemInventoryBinding
import java.text.NumberFormat
import java.util.Locale

class InventoryAdapter : ListAdapter<Product, InventoryAdapter.ViewHolder>(DiffCallback()) {

    var onEditClick: ((Product) -> Unit)? = null
    var onDeleteClick: ((Product) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.binding.btnEdit.setOnClickListener { onEditClick?.invoke(getItem(position)) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(getItem(position)) }
    }

    class ViewHolder(val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvProductPrice.text = formatPrice(product.price)
            binding.tvStock.text = "Stock: ${product.stock}"

            Glide.with(binding.root.context)
                .load(Constants.IMAGE_URL + product.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivProduct)
        }

        private fun formatPrice(price: String): String {
            val amount = price.toDoubleOrNull() ?: 0.0
            return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(amount)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}