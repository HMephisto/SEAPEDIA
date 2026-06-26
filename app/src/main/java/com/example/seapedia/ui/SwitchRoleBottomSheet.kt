package com.example.seapedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.seapedia.data.model.RoleConstants
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.BottomSheetSwitchRoleBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SwitchRoleBottomSheet (
    private val onRoleSelected: (role: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSwitchRoleBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    private val viewModel: SwitchRoleViewModel by viewModels {
        val sm = SessionManager(requireContext())
        val apiService = ApiClient.create { sm.getToken() }
        SwitchRoleViewModelFactory(AuthRepository(apiService), sm)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSwitchRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupRoleItems()
        observeViewModel()
    }

    private fun setupRoleItems() {
        val userRoles = sessionManager.getUserRoles()
        val currentRole = sessionManager.getActiveRole()

        val availableRoles = listOf(
            RoleConstants.BUYER,
            RoleConstants.SELLER,
            RoleConstants.DRIVER
        ).filter { it != currentRole }

        availableRoles.forEach { role ->
            val hasRole = userRoles.contains(role)
            val itemView = layoutInflater.inflate(
                com.example.seapedia.R.layout.item_role_selection,
                binding.rolesContainer,
                false
            )

            itemView.findViewById<android.widget.TextView>(
                com.example.seapedia.R.id.tvRoleName
            ).text = getRoleDisplayName(role, hasRole)

            itemView.findViewById<android.widget.TextView>(
                com.example.seapedia.R.id.tvRoleDesc
            ).text = getRoleDescription(role, hasRole)

            itemView.findViewById<android.widget.ImageView>(
                com.example.seapedia.R.id.ivRoleIcon
            ).setImageResource(getRoleIcon(role))

            itemView.findViewById<androidx.cardview.widget.CardView>(
                com.example.seapedia.R.id.cardRole
            ).setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(
                    requireContext(), getRoleColor(role)
                )
            )

            itemView.setOnClickListener {
                viewModel.handleRoleAction(role, hasRole)
            }

            binding.rolesContainer.addView(itemView)
        }

        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SwitchRoleState.Idle -> {}
                is SwitchRoleState.Loading -> {
                    binding.rolesContainer.isEnabled = false
                }

                is SwitchRoleState.Success -> {
                    dismiss()
                    onRoleSelected(state.role)
                }

                is SwitchRoleState.Error -> {
                    binding.rolesContainer.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
            }
        }
    }

    private fun getRoleDisplayName(role: String, hasRole: Boolean) = when (role) {
        RoleConstants.BUYER -> if (hasRole) "Switch to Buyer" else "Join as Buyer"
        RoleConstants.SELLER -> if (hasRole) "Switch to Seller" else "Join as Seller"
        RoleConstants.DRIVER -> if (hasRole) "Switch to Driver" else "Join as Driver"
        else -> role
    }

    private fun getRoleDescription(role: String, hasRole: Boolean) = when (role) {
        RoleConstants.BUYER -> if (hasRole) "Browse and order products" else "Add this role to your account"
        RoleConstants.SELLER -> if (hasRole) "Manage your store and products" else "Add this role to your account"
        RoleConstants.DRIVER -> if (hasRole) "Manage deliveries" else "Add this role to your account"
        else -> ""
    }

    private fun getRoleIcon(role: String) = when (role) {
        RoleConstants.BUYER -> com.example.seapedia.R.drawable.ic_buyer
        RoleConstants.SELLER -> com.example.seapedia.R.drawable.ic_seller
        RoleConstants.DRIVER -> com.example.seapedia.R.drawable.ic_driver
        else -> com.example.seapedia.R.drawable.ic_admin
    }

    private fun getRoleColor(role: String) = when (role) {
        RoleConstants.BUYER -> com.example.seapedia.R.color.role_buyer
        RoleConstants.SELLER -> com.example.seapedia.R.color.role_seller
        RoleConstants.DRIVER -> com.example.seapedia.R.color.role_driver
        else -> com.example.seapedia.R.color.primary
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}