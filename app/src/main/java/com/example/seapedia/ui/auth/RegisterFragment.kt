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
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentRegisterBinding
import com.example.seapedia.ui.buyer.BuyerMainActivity

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            viewModel.register(fullName, email, password, confirmPassword)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegisterUiState.Idle -> { }
                is RegisterUiState.Loading -> {
                    loadingDialog.show("Creating account...")
                }
                is RegisterUiState.Success -> {
                    loadingDialog.dismiss()
                    // register always defaults to BUYER
                    val intent = Intent(requireContext(), BuyerMainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                is RegisterUiState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}