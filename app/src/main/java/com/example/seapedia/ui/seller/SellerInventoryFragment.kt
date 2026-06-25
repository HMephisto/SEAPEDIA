package com.example.seapedia.ui.seller

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seapedia.R
import com.example.seapedia.data.network.ApiClient
import com.example.seapedia.data.repositrory.StoreRepository
import com.example.seapedia.data.utils.LoadingDialog
import com.example.seapedia.data.utils.SessionManager
import com.example.seapedia.databinding.FragmentGuestHomeBinding
import com.example.seapedia.databinding.FragmentSellerDashboardBinding
import com.example.seapedia.databinding.FragmentSellerInventoryBinding
import com.example.seapedia.ui.seller.adapter.InventoryAdapter


class SellerInventoryFragment : Fragment() {
    private var _binding: FragmentSellerInventoryBinding? = null
    private val binding get() = _binding!!

    private val inventoryAdapter = InventoryAdapter()
    private lateinit var loadingDialog: LoadingDialog
    private var storeId: Int = -1

    private val viewModel: SellerInventoryViewModel by viewModels {
        val sm = SessionManager(requireContext())
        val apiService = ApiClient.create { sm.getToken() }
        val repository = StoreRepository(apiService)
        SellerInventoryViewModelFactory(repository)
    }

    private val productFormLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.refresh()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingDialog = LoadingDialog(requireContext())

        val sellerDashboardViewModel: SellerDashboardViewModel by lazy {
            val sm = SessionManager(requireContext())
            val apiService = ApiClient.create { sm.getToken() }
            val repository = StoreRepository(apiService)
            androidx.lifecycle.ViewModelProvider(
                requireActivity(),
                SellerDashboardViewModelFactory(repository)
            )[SellerDashboardViewModel::class.java]
        }

        sellerDashboardViewModel.store.observe(viewLifecycleOwner) { store ->
            if (storeId == -1) {
                storeId = store.id
                viewModel.init(storeId)
            }
        }

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvInventory.apply {
            adapter = inventoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        inventoryAdapter.onEditClick = { product ->
            val intent = Intent(requireContext(), ProductFormActivity::class.java).apply {
                putExtra(ProductFormActivity.EXTRA_STORE_ID, storeId)
                putExtra(ProductFormActivity.EXTRA_PRODUCT, product)
            }
            productFormLauncher.launch(intent)
        }

        inventoryAdapter.onDeleteClick = { product ->
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete \"${product.name}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteProduct(product.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
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
        binding.fabAddProduct.setOnClickListener {
            val intent = Intent(requireContext(), ProductFormActivity::class.java).apply {
                putExtra(ProductFormActivity.EXTRA_STORE_ID, storeId)
            }
            productFormLauncher.launch(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            inventoryAdapter.submitList(products)
            binding.layoutEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            binding.rvInventory.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isRefreshing.observe(viewLifecycleOwner) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        viewModel.deleteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DeleteState.Idle -> { }
                is DeleteState.Loading -> loadingDialog.show("Deleting product...")
                is DeleteState.Success -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                    viewModel.resetDeleteState()
                }
                is DeleteState.Error -> {
                    loadingDialog.dismiss()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetDeleteState()
                }
            }
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