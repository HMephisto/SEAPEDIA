package com.example.seapedia.ui.buyer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.seapedia.data.model.Address
import com.example.seapedia.databinding.ItemAddressBinding

class AddressAdapter : ListAdapter<Address, AddressAdapter.ViewHolder>(DiffCallback()){
    var onEditClick: ((Address) -> Unit)? = null
    var onDeleteClick: ((Address) -> Unit)? = null
    var onSetDefaultClick: ((Address) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAddressBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.binding.btnEdit.setOnClickListener { onEditClick?.invoke(getItem(position)) }
        holder.binding.btnDelete.setOnClickListener { onDeleteClick?.invoke(getItem(position)) }
        holder.binding.btnSetDefault.setOnClickListener { onSetDefaultClick?.invoke(getItem(position)) }
    }

    class ViewHolder(val binding: ItemAddressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: Address) {
            binding.tvRecipientName.text = address.recipientName
            binding.tvPhone.text = address.phone
            binding.tvAddressDetail.text = address.addressDetail

            if (address.isDefault) {
                binding.tvDefaultBadge.visibility = View.VISIBLE
                binding.btnSetDefault.visibility = View.GONE
            } else {
                binding.tvDefaultBadge.visibility = View.GONE
                binding.btnSetDefault.visibility = View.VISIBLE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Address, newItem: Address) = oldItem == newItem
    }

}