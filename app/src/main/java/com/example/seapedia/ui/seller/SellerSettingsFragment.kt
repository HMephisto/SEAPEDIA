package com.example.seapedia.ui.seller

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.seapedia.data.model.RoleConstants
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.repositrory.StoreRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentSellerSettingsBinding
import com.example.seapedia.ui.SwitchRoleBottomSheet
import com.example.seapedia.ui.auth.AuthActivity
import com.example.seapedia.ui.auth.AuthViewModel
import com.example.seapedia.ui.auth.AuthViewModelFactory
import com.example.seapedia.ui.buyer.BuyerMainActivity
import com.example.seapedia.ui.splash.SplashActivity


class SellerSettingsFragment : Fragment() {
    private var _binding: FragmentSellerSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    private val viewModel: SellerSettingsViewModel by viewModels {
        val sm = SessionManager(requireContext())
        val apiService = ApiClient.create { sm.getToken() }
        SellerSettingsViewModelFactory(
            StoreRepository(apiService),
            AuthRepository(apiService),
            sm
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnUpdateStore.setOnClickListener {
            val storeName = binding.etStoreName.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            viewModel.updateStore(storeName, description, address)
        }

        binding.layoutSwitchRole.setOnClickListener {
            val bottomSheet = SwitchRoleBottomSheet { role ->
                navigateToRole(role)
            }
            bottomSheet.show(parentFragmentManager, "SwitchRole")
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout {
                val intent = Intent(requireContext(), AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.store.observe(viewLifecycleOwner) { store ->
            binding.etStoreName.setText(store.storeName)
            binding.etDescription.setText(store.description)
            binding.etAddress.setText(store.addressDetail)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SettingsState.Idle -> { }
                is SettingsState.Loading -> loadingDialog.show()
                is SettingsState.StoreUpdated -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Store updated!", Toast.LENGTH_SHORT).show()
                }
                is SettingsState.RoleSwitched -> {
                    loadingDialog.dismiss()
                    navigateToRole(state.role)
                }
                is SettingsState.RoleAdded -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Role added!", Toast.LENGTH_SHORT).show()
                    navigateToRole(state.role)
                }
                is SettingsState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToRole(role: String) {
        val destination = when (role) {
            RoleConstants.BUYER -> BuyerMainActivity::class.java
//            RoleConstants.DRIVER -> DriverMainActivity::class.java
            else -> BuyerMainActivity::class.java
        }
        val intent = Intent(requireContext(), destination)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}