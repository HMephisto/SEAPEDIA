package com.example.seapedia.ui.seller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.StoreRepository
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentGuestHomeBinding
import com.example.seapedia.databinding.FragmentSellerDashboardBinding
import com.example.seapedia.ui.seller.adapter.LowStockAdapter


class SellerDashboardFragment : Fragment() {
    private var _binding: FragmentSellerDashboardBinding? = null
    private val binding get() = _binding!!

    private val lowStockAdapter = LowStockAdapter()

    private val viewModel: SellerDashboardViewModel by viewModels {
        val sm = SessionManager(requireContext())
        val apiService = ApiClient.create { sm.getToken() }
        val repository = StoreRepository(apiService)
        SellerDashboardViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvLowStock.apply {
            adapter = lowStockAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setColorSchemeColors(
                ContextCompat.getColor(requireContext(), R.color.primary)
            )
            setOnRefreshListener { viewModel.refresh() }
        }
    }

    private fun setupClickListeners() {
        binding.tvSeeAllOrders.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigationView
            ).selectedItemId = R.id.sellerOrdersFragment
        }

        lowStockAdapter.onRestockClick = { product ->
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottomNavigationView
            ).selectedItemId = R.id.sellerInventoryFragment
        }
    }

    private fun observeViewModel() {
        viewModel.store.observe(viewLifecycleOwner) { store ->
            val initials = store.storeName
                .split(" ")
                .take(2)
                .joinToString("") { it.first().uppercase() }
            binding.tvStoreAvatar.text = initials
            binding.tvStoreName.text = store.storeName
            binding.tvStoreAddress.text = store.addressDetail
        }

        viewModel.products.observe(viewLifecycleOwner) { products ->
            binding.tvTotalProducts.text = products.size.toString()
        }

        viewModel.lowStockProducts.observe(viewLifecycleOwner) { lowStock ->
            if (lowStock.isEmpty()) {
                binding.cardLowStock.visibility = View.GONE
            } else {
                binding.cardLowStock.visibility = View.VISIBLE
                lowStockAdapter.submitList(lowStock)
            }
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}