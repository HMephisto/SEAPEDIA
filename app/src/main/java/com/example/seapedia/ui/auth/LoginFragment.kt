package com.example.seapedia.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.seapedia.R
import com.example.seapedia.data.model.RoleConstants
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentLoginBinding
import com.example.seapedia.ui.buyer.BuyerMainActivity
import com.example.seapedia.ui.seller.SellerMainActivity
import com.seacatering.app.ui.auth.RoleSelectionBottomSheet


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loadingDialog: LoadingDialog
    private val viewModel: AuthViewModel by viewModels {
        val sm = SessionManager(requireContext())
        val apiService = ApiClient.create { sm.getToken() }
        val repository = AuthRepository(apiService)
        AuthViewModelFactory(repository, sm)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginUiState.Idle -> {  }
                is LoginUiState.Loading -> {
                    loadingDialog.show("Logging in...")
                }
                is LoginUiState.Success -> {
                    loadingDialog.dismiss()
                    navigateToRoleActivity(state.role)
                }
                is LoginUiState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is LoginUiState.MultiRole -> {
                    loadingDialog.dismiss()
                    val bottomSheet = RoleSelectionBottomSheet(state.roles) { selectedRole ->
                        viewModel.switchRole(selectedRole)
                    }
                    bottomSheet.show(parentFragmentManager, "RoleSelection")
                }
            }
        }


    }

    private fun navigateToRoleActivity(role: String) {
        val destination = when (role) {
            RoleConstants.BUYER -> BuyerMainActivity::class.java
            RoleConstants.SELLER -> SellerMainActivity::class.java
//            RoleConstants.DRIVER -> DriverMainActivity::class.java
//            RoleConstants.ADMIN -> AdminMainActivity::class.java
            else -> BuyerMainActivity::class.java
        }
        startActivity(Intent(requireContext(), destination))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}