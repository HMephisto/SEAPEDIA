package com.example.seapedia.ui.buyer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.seapedia.R
import com.example.seapedia.data.model.RoleConstants
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.AuthRepository
import com.example.seapedia.data.repositrory.UserRepository
import com.example.seapedia.data.repositrory.WalletRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentBuyerProfileBinding
import com.example.seapedia.databinding.FragmentGuestHomeBinding
import com.example.seapedia.ui.SwitchRoleBottomSheet
import com.example.seapedia.ui.auth.AuthActivity
import com.example.seapedia.ui.auth.AuthViewModel
import com.example.seapedia.ui.auth.AuthViewModelFactory
import com.example.seapedia.ui.seller.SellerMainActivity
import com.example.seapedia.ui.splash.SplashActivity
import java.text.NumberFormat
import java.util.Locale
import kotlin.getValue


class BuyerProfileFragment : Fragment() {
    private var _binding: FragmentBuyerProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: LoadingDialog

    private val viewModel: BuyerProfileViewModel by viewModels {
        val sm = SessionManager(requireContext())
        val apiService = ApiClient.create { sm.getToken() }
        BuyerProfileViewModelFactory(
            UserRepository(apiService),
            WalletRepository(apiService),
            AuthRepository(apiService),
            sm
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.primary))
            setOnRefreshListener { viewModel.refresh() }
        }
    }

    private fun setupClickListeners() {
        binding.cardWallet.setOnClickListener {
            startActivity(Intent(requireContext(), WalletActivity::class.java))
        }

        binding.layoutAddresses.setOnClickListener {
            startActivity(Intent(requireContext(), AddressActivity::class.java))
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
        viewModel.me.observe(viewLifecycleOwner) { me ->
            val initials = me.fullName
                .split(" ").take(2)
                .joinToString("") { it.first().uppercase() }
            binding.tvAvatar.text = initials
            binding.tvFullName.text = me.fullName
            binding.tvEmail.text = me.email
            binding.tvActiveRole.text = me.activeRole.name
        }

        viewModel.wallet.observe(viewLifecycleOwner) { wallet ->
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            binding.tvWalletBalance.text = format.format(wallet.balance)
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Idle -> { }
                is ProfileState.Loading -> loadingDialog.show()
                is ProfileState.RoleSwitched -> {
                    loadingDialog.dismiss()
                    navigateToRole(state.role)
                }
                is ProfileState.RoleAdded -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Role added!", Toast.LENGTH_SHORT).show()
                    navigateToRole(state.role)
                }
                is ProfileState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToRole(role: String) {
        val destination = when (role) {
            RoleConstants.SELLER -> SellerMainActivity::class.java
//            RoleConstants.DRIVER -> DriverMainActivity::class.java
            else -> BuyerMainActivity::class.java
        }
        val intent = Intent(requireContext(), destination)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}