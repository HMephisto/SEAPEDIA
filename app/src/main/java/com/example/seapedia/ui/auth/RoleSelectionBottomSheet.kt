package com.seacatering.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.seapedia.R
import com.example.seapedia.data.model.RoleConstants
import com.example.seapedia.data.model.UserRole
import com.example.seapedia.databinding.BottomSheetRoleSelectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RoleSelectionBottomSheet(
    private val roles: List<UserRole>,
    private val onRoleSelected: (UserRole) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetRoleSelectionBinding? = null
    private val binding get() = _binding!!

    // prevent dismiss by tapping outside
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRoleItems()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRoleItems() {
        roles.forEach { role ->
            val itemView = layoutInflater.inflate(
                R.layout.item_role_selection,
                binding.rolesContainer,
                false
            )

            itemView.findViewById<TextView>(R.id.tvRoleName).text = getRoleDisplayName(role.name)
            itemView.findViewById<TextView>(R.id.tvRoleDesc).text = getRoleDescription(role.name)
            itemView.findViewById<ImageView>(R.id.ivRoleIcon).setImageResource(getRoleIcon(role.name))
            itemView.findViewById<CardView>(R.id.cardRole).setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), getRoleColor(role.name))
            )

            itemView.setOnClickListener {
                onRoleSelected(role)
                dismiss()
            }

            binding.rolesContainer.addView(itemView)
        }
    }

    private fun getRoleDisplayName(role: String) = when (role) {
        RoleConstants.BUYER  -> "Continue as Buyer"
        RoleConstants.SELLER -> "Continue as Seller"
        RoleConstants.DRIVER -> "Continue as Driver"
        RoleConstants.ADMIN  -> "Continue as Admin"
        else -> role
    }

    private fun getRoleDescription(role: String) = when (role) {
        RoleConstants.BUYER  -> "Browse marketplace and manage orders"
        RoleConstants.SELLER -> "Manage inventory, sales, and analytics"
        RoleConstants.DRIVER -> "Access route navigation and deliveries"
        RoleConstants.ADMIN  -> "System configuration and monitoring"
        else -> ""
    }

    private fun getRoleIcon(role: String) = when (role) {
        RoleConstants.BUYER  -> R.drawable.ic_buyer
        RoleConstants.SELLER -> R.drawable.ic_seller
        RoleConstants.DRIVER -> R.drawable.ic_driver
        RoleConstants.ADMIN  -> R.drawable.ic_admin
        else -> R.drawable.ic_buyer
    }

    private fun getRoleColor(role: String) = when (role) {
        RoleConstants.BUYER  -> R.color.role_buyer
        RoleConstants.SELLER -> R.color.role_seller
        RoleConstants.DRIVER -> R.color.role_driver
        RoleConstants.ADMIN  -> R.color.role_admin
        else -> R.color.blue
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}